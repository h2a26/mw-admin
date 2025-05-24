package org.hein.api.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hein.entity.Action;
import org.hein.entity.Permission;

/**
 * Request DTO for creating or updating a Permission
 */
@Builder
public record PermissionCreateRequest(
        @NotNull(message = "Feature ID is required")
        Long featureId,
        
        @NotBlank(message = "Action is required")
        String action,
        
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,
        
        Boolean requiresApproval,
        
        @Size(max = 1000, message = "Constraint policy cannot exceed 1000 characters")
        String constraintPolicy
) {
    /**
     * Convert request DTO to entity
     * Note: feature relationship needs to be set separately
     */
    public Permission toEntity() {
        return Permission.builder()
                .action(Action.valueOf(action.toUpperCase()))
                .description(description)
                .requiresApproval(requiresApproval != null ? requiresApproval : false)
                .constraintPolicy(constraintPolicy)
                .build();
    }
}
