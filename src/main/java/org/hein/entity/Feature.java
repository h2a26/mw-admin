package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "features", indexes = {
        @Index(name = "idx_feature_name", columnList = "name", unique = true),
        @Index(name = "idx_feature_path", columnList = "path", unique = true)
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

    @JsonIgnore
    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RoleFeature> roleFeatures = new HashSet<>();

    // Helper methods for role assignment
    public void addRole(Role role, Set<String> allowedActions) {
        RoleFeature roleFeature = new RoleFeature(role, this, allowedActions);
        roleFeatures.add(roleFeature);
    }

    public void removeRole(Role role) {
        roleFeatures.removeIf(rf -> rf.getRole().equals(role));
    }
}
