package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Builder
@Entity
@Table(name = "role_features",
        indexes = {
                @Index(name = "idx_role_feature", columnList = "role_id,feature_id", unique = true)
        }
)
@Cache(region = "roleFeature", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
public class RoleFeature extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnore
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @ElementCollection
    @CollectionTable(
            name = "role_feature_permissions",
            joinColumns = {
                    @JoinColumn(name = "role_feature_id", referencedColumnName = "id")
            }
    )
    @Column(name = "permission_name")
    @Builder.Default
    private Set<String> allowedActions = new HashSet<>();

    @Builder
    public RoleFeature(Role role, Feature feature, Set<String> allowedActions) {
        this.role = role;
        this.feature = feature;
        this.allowedActions = allowedActions != null ? allowedActions : new HashSet<>();
    }

    public void addAction(String action) {
        allowedActions.add(action);
    }

    public void removeAction(String action) {
        allowedActions.remove(action);
    }

    public boolean hasAction(String action) {
        return allowedActions.contains(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleFeature that = (RoleFeature) o;
        return Objects.equals(role.getId(), that.role.getId()) &&
                Objects.equals(feature.getId(), that.feature.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(role.getId(), feature.getId());
    }
}

