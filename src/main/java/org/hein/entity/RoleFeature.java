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
@Table(name = "role_features",
        indexes = {
                @Index(name = "idx_role_feature", columnList = "role_id,feature_id", unique = true)
        }
)
@Cache(region = "roleFeature", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleFeature extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnore // Avoid recursion roleFeature -> role -> roleFeature
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch to avoid lazy issues on actions
    @CollectionTable(
            name = "role_feature_actions",
            joinColumns = {
                    @JoinColumn(name = "role_feature_id", referencedColumnName = "id")
            }
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    @Builder.Default
    private Set<Action> allowedActions = new HashSet<>();

    public void addAction(Action action) {
        allowedActions.add(action);
    }

    public void removeAction(Action action) {
        allowedActions.remove(action);
    }

    public boolean hasAction(Action action) {
        return allowedActions.contains(action);
    }

    public Set<String> getAllowedActionNames() {
        return allowedActions.stream()
                .map(Action::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleFeature that)) return false;
        return Objects.equals(role.getId(), that.role.getId()) &&
                Objects.equals(feature.getId(), that.feature.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(role.getId(), feature.getId());
    }

    public static RoleFeature create(Role role, Feature feature, Action... actions) {
        return RoleFeature.builder()
                .role(role)
                .feature(feature)
                .allowedActions(new HashSet<>(Arrays.asList(actions)))
                .build();
    }
}
