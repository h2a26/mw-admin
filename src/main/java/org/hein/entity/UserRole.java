package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_roles",
        indexes = {
                @Index(name = "idx_user_role", columnList = "user_id,role_id", unique = true)
        }
)
@Cache(region = "userRole", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Role role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", updatable = false)
    private User assignedBy;

    @Column(updatable = false)
    private String assignmentReason;

    @Builder.Default
    private boolean active = true;

    // Constructor for builder
    @Builder
    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return Objects.equals(user.getId(), userRole.user.getId()) &&
                Objects.equals(role.getId(), userRole.role.getId());
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

