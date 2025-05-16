package org.hein.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "features", indexes = {
        @Index(name = "idx_feature_name", columnList = "name", unique = true),
        @Index(name = "idx_feature_path", columnList = "path", unique = true),
        @Index(name = "idx_feature_parent", columnList = "parent_id")
})
@Cache(region = "feature", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature extends AuditableEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String path;

    private String icon;
    private String description;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private boolean isSystemFeature = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Feature parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<Feature> children = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RoleFeature> roleFeatures = new HashSet<>();

    // Get all permissions for this feature
    @Getter
    @JsonIgnore
    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @Transient
    @JsonIgnore
    private Set<Action> allowedActions;

    // Get all actions (with caching)
    @JsonIgnore
    public Set<Action> getAllowedActions() {
        if (this.allowedActions == null) {
            this.allowedActions = roleFeatures.stream()
                    .flatMap(rf -> rf.getAllowedActions().stream())
                    .collect(Collectors.toSet());
        }
        return this.allowedActions;
    }

    // Get all roles with access to this feature
    public Set<Role> getRoles() {
        return roleFeatures.stream()
                .map(RoleFeature::getRole)
                .collect(Collectors.toSet());
    }

    // Check if feature is accessible by role
    public boolean isAccessibleBy(Role role) {
        return roleFeatures.stream()
                .anyMatch(rf -> rf.getRole().equals(role));
    }

    // Add permission
    public void addPermission(Permission permission) {
        if (permissions.stream().noneMatch(p -> p.equals(permission))) {
            permissions.add(permission);
        }
    }

    // Remove permission
    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    // Add role with actions
    public void addRole(Role role, Action... actions) {
        RoleFeature roleFeature = roleFeatures.stream()
                .filter(rf -> rf.getRole().equals(role))
                .findFirst()
                .orElse(null);

        if (roleFeature == null) {
            roleFeature = RoleFeature.create(role, this, actions);
            roleFeatures.add(roleFeature);
        } else {
            for (Action action : actions) {
                roleFeature.addAction(action);
            }
        }
    }

    // Remove role
    public void removeRole(Role role) {
        roleFeatures.removeIf(rf -> rf.getRole().equals(role));
    }

    // Get all child features recursively
    public Set<Feature> getAllChildren() {
        Set<Feature> allChildren = new HashSet<>(children);
        children.forEach(child -> allChildren.addAll(child.getAllChildren()));
        return allChildren;
    }

    // Get all parent features recursively
    public Set<Feature> getAllParents() {
        Set<Feature> allParents = new HashSet<>();
        Feature current = this.parent;
        while (current != null) {
            allParents.add(current);
            current = current.getParent();
        }
        return allParents;
    }

    // Check if feature is a child of another feature
    public boolean isChildOf(Feature parent) {
        return getAllParents().contains(parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return name.equals(feature.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static Feature create(String name, String path, String icon, String description, Integer displayOrder) {
        return Feature.builder()
                .name(name)
                .path(path)
                .icon(icon)
                .description(description)
                .displayOrder(displayOrder)
                .build();
    }
}
