package org.hein.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_feature_action", columnList = "feature_id,action", unique = true)
})
@Cache(region = "permission", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends AuditableEntity {

    /**
     * The feature this permission belongs to
     * Immutable after creation
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feature_id", nullable = false, updatable = false)
    private Feature feature;

    /**
     * The action this permission allows on the feature
     * Immutable after creation
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, updatable = false)
    private Action action;

    /**
     * Human-readable description of the permission
     */
    @Column(length = 255)
    private String description;
    
    /**
     * Determines if this permission requires additional authorization
     * beyond regular role check (e.g., for sensitive operations)
     */
    @Builder.Default
    @Column(name = "requires_approval")
    private boolean requiresApproval = false;
    
    /**
     * Policy or constraint for this permission (can be a JSON or expression)
     */
    @Column(name = "constraint_policy", length = 1000)
    private String constraintPolicy;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    @BatchSize(size = 20)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Role> roles = new HashSet<>();
    
    /**
     * Returns the canonical name of this permission in format: feature_code:ACTION
     */
    @Transient  // Not persisted, dynamically generated
    public String getPermissionName() {
        return feature.getCode() + ":" + action.name();
    }
    
    /**
     * Returns a user-friendly display name for this permission
     */
    @Transient
    public String getDisplayName() {
        return feature.getName() + " - " + getActionDisplayName();
    }
    
    /**
     * Returns a user-friendly name for the action
     */
    @Transient
    private String getActionDisplayName() {
        return action.name().charAt(0) + action.name().substring(1).toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission that)) return false;
        return feature != null && action != null &&
               feature.equals(that.feature) && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature, action);
    }
}