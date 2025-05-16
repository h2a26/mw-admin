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
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean expired = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean credentialsExpired = false;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @Transient
    @JsonIgnore
    private Set<Role> roles;

    // Get all roles (with caching)
    @JsonIgnore
    public Set<Role> getAllRoles() {
        if (this.roles == null) {
            this.roles = userRoles.stream()
                    .map(UserRole::getRole)
                    .collect(Collectors.toSet());
        }
        return this.roles;
    }

    // Add role helper
    public void addRole(Role role) {
        if (userRoles.stream().noneMatch(ur -> ur.getRole().equals(role))) {
            userRoles.add(UserRole.create(this, role));
            this.roles = null; // Invalidate cache
        }
    }

    // Remove role helper
    public void removeRole(Role role) {
        userRoles.removeIf(ur -> ur.getRole().equals(role));
        this.roles = null; // Invalidate cache
    }

    // Check if user has permission for feature
    public boolean hasPermission(Feature feature, Action action) {
        return getAllRoles().stream()
                .anyMatch(role -> role.hasPermission(feature, action));
    }

    // Get all features accessible by this user
    public Set<Feature> getAccessibleFeatures() {
        return getAllRoles().stream()
                .flatMap(role -> role.getAccessibleFeatures().stream())
                .collect(Collectors.toSet());
    }

    // Get all actions for a feature
    public Set<Action> getActionsForFeature(Feature feature) {
        return getAllRoles().stream()
                .map(role -> role.getActionsForFeature(feature))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    // Get all permissions for a feature
    public Set<Permission> getPermissionsForFeature(Feature feature) {
        return getAllRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission.getFeature().equals(feature))
                .collect(Collectors.toSet());
    }

    // Get all permissions across all features
    public Set<Permission> getAllPermissions() {
        return getAllRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public static User create(String username, String password, String email, String firstName, String lastName) {
        return User.builder()
                .username(username)
                .password(password)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }
}
