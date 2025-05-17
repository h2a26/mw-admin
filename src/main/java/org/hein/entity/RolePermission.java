package org.hein.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

@Entity
@Table(name = "role_permissions",
        indexes = {
                @Index(name = "idx_role_permission", columnList = "role_id,permission_id", unique = true)
        }
)
@Cache(region = "rolePermission", usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnore // Avoid recursion rolePermission -> role -> rolePermission
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermission that)) return false;
        return Objects.equals(role.getId(), that.role.getId()) &&
                Objects.equals(permission.getId(), that.permission.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(role.getId(), permission.getId());
    }
}
