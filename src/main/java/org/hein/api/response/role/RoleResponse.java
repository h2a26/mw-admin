package org.hein.api.response.role;

import org.hein.entity.Role;
import org.hein.utils.AuditableEntity;

import java.util.Set;
import java.util.stream.Collectors;

public record RoleResponse(
        Long id,
        String name,
        String description,
        Set<Long> permissionIds
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(AuditableEntity::getId)
                        .collect(Collectors.toSet())
        );
    }
}
