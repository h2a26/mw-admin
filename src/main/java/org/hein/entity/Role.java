package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
})
@Cache(region = "role", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "Role.withPermissions",
                attributeNodes = @NamedAttributeNode("rolePermissions")
        )
})
public class Role extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Builder.Default
    private boolean systemRole = false;

    @JsonIgnore
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RoleFeature> roleFeatures = new HashSet<>();

    @Transient
    @JsonIgnore
    private Set<Permission> permissions;

    /**
     * Fetch permissions cached, call within transaction to avoid lazy issues
     */
    @JsonIgnore
    public Set<Permission> getPermissions() {
        if (this.permissions == null) {
            this.permissions = rolePermissions.stream()
                    .map(RolePermission::getPermission)
                    .collect(Collectors.toSet());
        }
        return this.permissions;
    }

    // Add permission helper
    public void addPermission(Permission permission) {
        if (rolePermissions.stream().noneMatch(rp -> rp.getPermission().equals(permission))) {
            rolePermissions.add(RolePermission.builder().role(this).permission(permission).build());
            this.permissions = null; // invalidate cache
        }
    }

    // Remove permission helper
    public void removePermission(Permission permission) {
        rolePermissions.removeIf(rp -> rp.getPermission().equals(permission));
        this.permissions = null; // invalidate cache
    }

    // Add feature with actions
    public void addFeature(Feature feature, Action... actions) {
        RoleFeature roleFeature = roleFeatures.stream()
                .filter(rf -> rf.getFeature().equals(feature))
                .findFirst()
                .orElse(null);

        if (roleFeature == null) {
            roleFeature = RoleFeature.create(this, feature, actions);
            roleFeatures.add(roleFeature);
        } else {
            for (Action action : actions) {
                roleFeature.addAction(action);
            }
        }
    }

    // Remove feature
    public void removeFeature(Feature feature) {
        roleFeatures.removeIf(rf -> rf.getFeature().equals(feature));
    }

    // Check permission
    public boolean hasPermission(Feature feature, Action action) {
        return roleFeatures.stream()
                .filter(rf -> rf.getFeature().equals(feature))
                .anyMatch(rf -> rf.hasAction(action));
    }

    // Get accessible features
    public Set<Feature> getAccessibleFeatures() {
        return roleFeatures.stream()
                .map(RoleFeature::getFeature)
                .collect(Collectors.toSet());
    }

    // Get allowed actions for feature
    public Set<Action> getActionsForFeature(Feature feature) {
        return roleFeatures.stream()
                .filter(rf -> rf.getFeature().equals(feature))
                .findFirst()
                .map(RoleFeature::getAllowedActions)
                .orElse(Collections.emptySet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
