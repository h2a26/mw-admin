package org.hein.api.response.permission;

import org.hein.api.response.feature.FeatureResponse;
import org.hein.entity.Permission;

import java.time.LocalDateTime;

/**
 * Response DTO for Permission entity
 */
public record PermissionResponse(
        Long id,
        String name,
        String action,
        String description,
        boolean requiresApproval,
        String constraintPolicy,
        FeatureResponse feature,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Convert Permission entity to response DTO
     */
    public static PermissionResponse fromEntity(Permission permission) {
        if (permission == null) {
            return null;
        }
        
        return new PermissionResponse(
                permission.getId(),
                permission.getPermissionName(),
                permission.getAction().name(),
                permission.getDescription(),
                permission.isRequiresApproval(),
                permission.getConstraintPolicy(),
                FeatureResponse.fromEntity(permission.getFeature(), false),  // Don't include feature children
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
