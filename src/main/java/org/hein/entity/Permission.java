package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

@Entity
@Table(name = "permissions",
        indexes = {
                @Index(name = "idx_permission_feature_action", columnList = "feature_id,action", unique = true)
        }
)
@Cache(region = "permission", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    @JsonIgnore // Avoid recursion when serializing permission -> feature -> permission
    private Feature feature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission that)) return false;
        return Objects.equals(feature.getId(), that.feature.getId()) &&
                action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature.getId(), action);
    }
}
