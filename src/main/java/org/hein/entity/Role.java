package org.hein.entity;

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

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true),
        @Index(name = "idx_role_code", columnList = "code", unique = true),
        @Index(name = "idx_role_parent_id", columnList = "parent_id")
})
@Cache(region = "role", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraph(
        name = "Role.withPermissions",
        attributeNodes = @NamedAttributeNode("permissions")
)
@NamedEntityGraph(
        name = "Role.withPermissionsAndParent",
        attributeNodes = {
                @NamedAttributeNode("permissions"),
                @NamedAttributeNode("parent")
        }
)
public class Role extends AuditableEntity {

    /**
     * Human-readable name of the role
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * Unique code for programmatic access, typically lowercase with underscores
     */
    @NaturalId
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Role description
     */
    @Column(length = 255)
    private String description;
    
    /**
     * Priority level for conflict resolution (higher value = higher priority)
     */
    @Builder.Default
    private Integer priority = 0;

    /**
     * Indicates if this is a system-defined role that cannot be modified/deleted
     */
    @Builder.Default
    @Column(name = "system_role")
    private boolean systemRole = false;
    
    /**
     * Indicates if this role is automatically assigned to new users
     */
    @Builder.Default
    @Column(name = "default_role")
    private boolean defaultRole = false;
    
    /**
     * Optional parent role for role hierarchy
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Role parent;
    
    /**
     * Expiration date for this role (null means no expiration)
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            indexes = {
                    @Index(name = "idx_role_permission", columnList = "role_id,permission_id")
            }
    )
    @Builder.Default
    @BatchSize(size = 30)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Permission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UserRole> userRoles = new HashSet<>();
    
    /**
     * Child roles in the role hierarchy
     */
    @OneToMany(mappedBy = "parent")
    @Builder.Default
    @BatchSize(size = 10)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Role> childRoles = new HashSet<>();

    // Helper methods
    /**
     * Add permission to this role
     */
    public void addPermission(Permission permission) {
        if (permissions.add(permission)) {
            permission.getRoles().add(this);
        }
    }

    /**
     * Remove permission from this role
     */
    public void removePermission(Permission permission) {
        if (permissions.remove(permission)) {
            permission.getRoles().remove(this);
        }
    }
    
    /**
     * Add a child role to this role
     */
    public void addChildRole(Role childRole) {
        childRoles.add(childRole);
        childRole.setParent(this);
    }
    
    /**
     * Remove a child role from this role
     */
    public void removeChildRole(Role childRole) {
        childRoles.remove(childRole);
        childRole.setParent(null);
    }
    
    /**
     * Get all permissions including those from parent roles
     */
    @Transient
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>(permissions);
        
        // Add parent permissions if present
        Role currentParent = this.parent;
        while (currentParent != null) {
            allPermissions.addAll(currentParent.getPermissions());
            currentParent = currentParent.getParent();
        }
        
        return allPermissions;
    }

    /**
     * Check if this role has a specific permission directly or through inheritance
     */
    public boolean hasPermission(String permissionName) {
        // Check direct permissions first for efficiency
        if (permissions.stream().anyMatch(p -> p.getPermissionName().equals(permissionName))) {
            return true;
        }
        
        // Check parent role permissions if not found directly
        return parent != null && parent.hasPermission(permissionName);
    }
    
    /**
     * Check if this role is active (not expired)
     */
    @Transient
    public boolean isActive() {
        return expiryDate == null || expiryDate.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return code != null && code.equals(role.getCode());
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}