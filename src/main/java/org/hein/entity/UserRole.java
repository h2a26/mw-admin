package org.hein.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    private User assignedBy;

    private String assignmentReason;

    @Builder.Default
    private boolean active = true;

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole userRole)) return false;
        return user.equals(userRole.user) &&
                role.equals(userRole.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, role);
    }
}