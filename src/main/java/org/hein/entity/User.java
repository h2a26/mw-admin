package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_enabled", columnList = "enabled"),
        @Index(name = "idx_user_locked", columnList = "locked")
})
@Cache(region = "user", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraph(
        name = "User.withRoles",
        attributeNodes = @NamedAttributeNode("userRoles")
)
@NamedEntityGraph(
        name = "User.withRolesAndPermissions",
        attributeNodes = {
            @NamedAttributeNode(value = "userRoles", subgraph = "userRoles.role")
        },
        subgraphs = {
            @NamedSubgraph(name = "userRoles.role", attributeNodes = @NamedAttributeNode("role"))
        }
)
public class User extends AuditableEntity {

    /**
     * Unique username for login
     */
    @Column(nullable = false, unique = true, length = 50)
    @NaturalId
    private String username;

    /**
     * User's first name
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * User's last name
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Hashed password
     */
    @Column(nullable = false)
    @JsonIgnore
    private String password;

    /**
     * When the password was last changed
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    /**
     * When the password expires and must be reset
     */
    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;
    
    /**
     * Number of previous passwords to remember (prevent reuse)
     */
    @Column(name = "password_history_count")
    private Integer passwordHistoryCount;
    
    /**
     * Count of failed login attempts
     */
    @Builder.Default
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;
    
    /**
     * When the account will be automatically unlocked after too many failed attempts
     */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /**
     * User's email address
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * Optional mobile phone number
     */
    @Column(length = 20)
    private String mobilePhone;
    
    /**
     * Two-factor authentication enabled
     */
    @Builder.Default
    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;
    
    /**
     * Last login timestamp
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * IP address of last login
     */
    @Column(name = "last_login_ip", length = 45)  // IPv6 compatible
    private String lastLoginIp;

    /**
     * Whether this account is enabled
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Whether this account is locked
     */
    @Builder.Default
    private boolean locked = false;
    
    /**
     * Whether this is a system/service account (not tied to a real person)
     */
    @Builder.Default
    @Column(name = "system_account")
    private boolean systemAccount = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UserRole> userRoles = new HashSet<>();

    /**
     * Add a role to this user
     */
    public void addRole(Role role) {
        if (getRoles().stream().noneMatch(r -> r.equals(role))) {
            UserRole userRole = new UserRole(this, role);
            userRoles.add(userRole);
            role.getUserRoles().add(userRole);
        }
    }

    /**
     * Add a role with specific assignment metadata
     */
    public void addRole(Role role, User assignedBy, String reason, LocalDateTime validTo) {
        if (getRoles().stream().noneMatch(r -> r.equals(role))) {
            UserRole userRole = UserRole.builder()
                    .user(this)
                    .role(role)
                    .assignedBy(assignedBy)
                    .assignmentReason(reason)
                    .assignedAt(LocalDateTime.now())
                    .validFrom(LocalDateTime.now())
                    .validTo(validTo)
                    .status(assignedBy != null ? UserRoleStatus.ACTIVE : UserRoleStatus.PENDING)
                    .build();
            
            userRoles.add(userRole);
            role.getUserRoles().add(userRole);
        }
    }

    /**
     * Remove a role from this user
     */
    public void removeRole(Role role) {
        userRoles.removeIf(userRole -> {
            if (userRole.getRole().equals(role)) {
                role.getUserRoles().remove(userRole);
                return true;
            }
            return false;
        });
    }

    /**
     * Get all roles assigned to this user
     */
    public Set<Role> getRoles() {
        return userRoles.stream()
                .filter(UserRole::isValid)  // Only include valid role assignments
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

    /**
     * Get all permissions from all roles, including inherited permissions
     */
    public Set<Permission> getPermissions() {
        Set<Permission> allPermissions = new HashSet<>();
        
        // Add permissions from roles
        getRoles().forEach(role -> {
            // Get permissions including those inherited from parent roles
            allPermissions.addAll(role.getAllPermissions());
        });
        
        return allPermissions;
    }

    /**
     * Check if this user has a specific permission
     */
    public boolean hasPermission(String permissionName) {
        // Check role permissions
        return getRoles().stream()
                .anyMatch(role -> role.hasPermission(permissionName));
    }

    /**
     * Check if this user has any of the specified permissions
     */
    public boolean hasAnyPermission(Set<String> permissionNames) {
        // Get all permission names for more efficient checking
        Set<String> userPermissionNames = new HashSet<>();

        // Add role permission names
        getRoles().forEach(role -> 
            role.getAllPermissions().forEach(permission -> 
                userPermissionNames.add(permission.getPermissionName())
            )
        );
        
        return permissionNames.stream().anyMatch(userPermissionNames::contains);
    }
    
    /**
     * Record a successful login
     */
    public void recordSuccessfulLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }
    
    /**
     * Record a failed login attempt and potentially lock account
     */
    public void recordFailedLogin(int maxAttempts, int lockDurationMinutes) {
        this.failedAttempts++;
        
        if (this.failedAttempts >= maxAttempts) {
            this.locked = true;
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        }
    }
    
    /**
     * Check if password needs to be changed
     */
    @Transient
    public boolean isPasswordExpired() {
        return passwordExpiresAt != null && LocalDateTime.now().isAfter(passwordExpiresAt);
    }
    
    /**
     * Set a new password and update expiry based on policy
     */
    public void updatePassword(String newEncodedPassword, int expiryDays) {
        this.password = newEncodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
        
        if (expiryDays > 0) {
            this.passwordExpiresAt = LocalDateTime.now().plusDays(expiryDays);
        } else {
            this.passwordExpiresAt = null;  // No expiration
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return username != null && username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}