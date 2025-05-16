package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Cache(region = "user", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "User.withRoles",
                attributeNodes = @NamedAttributeNode("userRoles")
        )
})
public class User extends AuditableEntity {
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String firstName;
    private String lastName;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Builder.Default
    private boolean mfaEnabled = false;

    private String mfaSecret;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @Transient
    @JsonIgnore
    private Set<Role> roles;

    @Transient
    @JsonIgnore
    private Set<Permission> permissions;

    // Helper method to get all roles (with caching)
    @JsonIgnore
    public Set<Role> getAllRoles() {
        if (this.roles == null) {
            this.roles = userRoles.stream()
                    .map(UserRole::getRole)
                    .collect(Collectors.toSet());
        }
        return this.roles;
    }

    // Helper method to get all permissions (with caching)
    @JsonIgnore
    public Set<Permission> getAllPermissions() {
        if (this.permissions == null) {
            this.permissions = getAllRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .collect(Collectors.toSet());
        }
        return this.permissions;
    }

    // Add role helper
    public void addRole(Role role) {
        if (userRoles.stream().noneMatch(ur -> ur.getRole().equals(role))) {
            userRoles.add(new UserRole(this, role));
            this.roles = null; // Invalidate cache
            this.permissions = null;
        }
    }

    // Remove role helper
    public void removeRole(Role role) {
        userRoles.removeIf(ur -> ur.getRole().equals(role));
        this.roles = null; // Invalidate cache
        this.permissions = null;
    }

    // Check if user has permission
    public boolean hasPermission(String permissionName) {
        return getAllPermissions().stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }
}
