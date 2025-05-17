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
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Cache(region = "user", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean locked = false;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @Transient
    @JsonIgnore
    private Set<Role> allRolesCache;

    /**
     * Get all roles assigned to the user.
     * Should be called inside transaction/session to avoid lazy loading issues.
     */
    @JsonIgnore
    public Set<Role> getAllRoles() {
        if (allRolesCache == null) {
            allRolesCache = userRoles.stream()
                    .map(UserRole::getRole)
                    .collect(Collectors.toSet());
        }
        return allRolesCache;
    }

    /**
     * Check if user has permission for a feature and action by checking all roles.
     */
    public boolean hasPermission(Feature feature, Action action) {
        return getAllRoles().stream()
                .anyMatch(role -> role.hasPermission(feature, action));
    }

    /**
     * Get all features accessible by user.
     */
    public Set<Feature> getAccessibleFeatures() {
        return getAllRoles().stream()
                .flatMap(role -> role.getAccessibleFeatures().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Get all actions user can perform on a feature.
     */
    public Set<Action> getActionsForFeature(Feature feature) {
        return getAllRoles().stream()
                .flatMap(role -> role.getActionsForFeature(feature).stream())
                .collect(Collectors.toSet());
    }

    // Add role helper
    public void addRole(Role role) {
        if (userRoles.stream().noneMatch(ur -> ur.getRole().equals(role))) {
            userRoles.add(UserRole.create(this, role));
            allRolesCache = null; // Invalidate cache
        }
    }

    public void clearRoles() {
        for (UserRole userRole : new HashSet<>(userRoles)) {
            userRole.getRole().getUserRoles().remove(userRole);
        }
        userRoles.clear();
    }

    // Remove role helper
    public void removeRole(Role role) {
        userRoles.removeIf(ur -> ur.getRole().equals(role));
        allRolesCache = null; // Invalidate cache
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}