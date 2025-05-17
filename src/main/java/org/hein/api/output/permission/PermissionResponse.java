package org.hein.api.output.permission;

import org.hein.entity.Permission;

public record PermissionResponse(
        Long id,
        Long featureId,
        String action
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getFeature().getId(),
                permission.getAction().name()
        );
    }
}
