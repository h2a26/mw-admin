package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;
import java.util.*;

@Builder
@AllArgsConstructor
@Entity
@Table(name = "user_roles",
        indexes = {
                @Index(name = "idx_user_role", columnList = "user_id,role_id", unique = true),
                @Index(name = "idx_user_role_status", columnList = "user_id,status")
        }
)
@Cache(region = "userRole", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
public class UserRole extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    @JsonIgnore
    private User assignedBy;

    @Column(updatable = false)
    private String assignmentReason;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean inheritPermissions = true;

    public static UserRole create(User user, Role role) {
        return UserRole.builder()
                .user(user)
                .role(role)
                .active(true)
                .inheritPermissions(true)
                .build();
    }

    // Get all permissions for this user-role combination
    public Set<Permission> getPermissions() {
        return role.getPermissions();
    }

    // Get all features accessible by this user-role combination
    public Set<Feature> getAccessibleFeatures() {
        return role.getAccessibleFeatures();
    }

    // Check if this user-role combination has permission for a feature
    public boolean hasPermission(Feature feature, Action action) {
        return role.hasPermission(feature, action);
    }

    // Get all actions for a feature
    public Set<Action> getActionsForFeature(Feature feature) {
        return role.getActionsForFeature(feature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole that = (UserRole) o;
        return Objects.equals(user.getId(), that.user.getId()) &&
                Objects.equals(role.getId(), that.role.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId(), role.getId());
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + getId() +
                ", userId=" + (user != null ? user.getId() : null) +
                ", roleId=" + (role != null ? role.getId() : null) +
                ", assignedAt=" + assignedAt +
                '}';
    }
}
