package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
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
@NamedEntityGraph(
        name = "User.withRoles",
        attributeNodes = @NamedAttributeNode("userRoles")
)
public class User extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean locked = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UserRole> userRoles = new HashSet<>();

    // Helper methods
    public void addRole(Role role) {
        UserRole userRole = new UserRole(this, role);
        userRoles.add(userRole);
        role.getUserRoles().add(userRole);
    }

    public void removeRole(Role role) {
        UserRole userRole = new UserRole(this, role);
        role.getUserRoles().remove(userRole);
        userRoles.remove(userRole);
    }

    public boolean hasRole(Role role) {
        return userRoles.stream()
                .anyMatch(ur -> ur.getRole().equals(role));
    }

    public Set<Role> getRoles() {
        return userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

    public Set<Permission> getPermissions() {
        return getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public boolean hasPermission(Permission permission) {
        return getPermissions().contains(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}