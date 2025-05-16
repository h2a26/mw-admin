package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "name", unique = true),
        @Index(name = "idx_permission_resource_action", columnList = "resource,action", unique = true),
        @Index(name = "idx_permission_feature", columnList = "feature_id")
})
@Cache(region = "permission", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends AuditableEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String resource;

    @Enumerated(EnumType.STRING)
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    @JsonIgnore
    private Feature feature;

    private String description;

    // Composite key for cache
    @Transient
    @JsonIgnore
    public String getPermissionKey() {
        return resource + ":" + action.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(name, that.name) ||
                (Objects.equals(resource, that.resource) &&
                        Objects.equals(action, that.action) &&
                        Objects.equals(feature.getId(), that.feature.getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resource, action, feature.getId());
    }

    public static Permission create(String resource, Action action, Feature feature, String description) {
        return Permission.builder()
                .name(resource + ":" + action.getName())
                .resource(resource)
                .action(action)
                .feature(feature)
                .description(description)
                .build();
    }

}