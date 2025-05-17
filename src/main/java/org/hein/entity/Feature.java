package org.hein.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "features", indexes = {
        @Index(name = "idx_feature_name", columnList = "name", unique = true)
})
@Cache(region = "feature", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "Feature.withPermissions",
                attributeNodes = @NamedAttributeNode("permissions")
        )
})
public class Feature extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Builder.Default
    private boolean systemFeature = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_feature_id")
    @JsonBackReference // Fix: prevents infinite recursion when serializing parent/child hierarchy
    private Feature parentFeature;

    @OneToMany(mappedBy = "parentFeature", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Fix: paired with @JsonBackReference on parentFeature
    @Builder.Default
    private Set<Feature> childFeatures = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @Transient
    @JsonIgnore
    private Set<Action> actions;

    // Cache actions from permissions, call within transaction/session to avoid LazyInitializationException
    @JsonIgnore
    public Set<Action> getActions() {
        if (actions == null) {
            actions = permissions.stream()
                    .map(Permission::getAction)
                    .collect(Collectors.toSet());
        }
        return actions;
    }

    // Add permission helper method
    public void addPermission(Permission permission) {
        if (permissions.stream().noneMatch(p -> p.equals(permission))) {
            permissions.add(permission);
            this.actions = null; // Invalidate cache
        }
    }

    // Remove permission helper method
    public void removePermission(Permission permission) {
        permissions.remove(permission);
        this.actions = null; // Invalidate cache
    }
}
