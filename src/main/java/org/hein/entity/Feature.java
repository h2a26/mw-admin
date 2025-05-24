package org.hein.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "features", indexes = {
        @Index(name = "idx_feature_code", columnList = "code", unique = true),
        @Index(name = "idx_feature_parent_id", columnList = "parent_id")
})
@Cache(region = "feature", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * A unique code for programmatic reference, typically lowercase with underscores
     */
    @NaturalId
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Builder.Default
    private boolean enabled = true;
    
    /**
     * Display order for UI presentation
     */
    private Integer displayOrder;
    
    /**
     * Feature icon or identifier for UI
     */
    @Column(length = 50)
    private String icon;
    
    /**
     * Optional parent feature for hierarchical organization
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Feature parent;
    
    /**
     * Child features in the hierarchy
     */
    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    @OrderBy("displayOrder ASC, name ASC")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Feature> children = new ArrayList<>();

    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Permission> permissions = new HashSet<>();

    // Helper methods
    public void addPermission(Permission permission) {
        permission.setFeature(this);
        permissions.add(permission);
    }
    
    /**
     * Add a child feature to this feature
     */
    public void addChild(Feature child) {
        children.add(child);
        child.setParent(this);
    }
    
    /**
     * Remove a child feature from this feature
     */
    public void removeChild(Feature child) {
        children.remove(child);
        child.setParent(null);
    }
    
    /**
     * Check if this feature is a top-level feature (has no parent)
     */
    public boolean isTopLevel() {
        return parent == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feature feature)) return false;
        return code != null && code.equals(feature.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}