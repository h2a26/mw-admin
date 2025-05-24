package org.hein.api.request.userrole;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hein.entity.UserRole;
import org.hein.entity.UserRoleStatus;

import java.time.LocalDateTime;

/**
 * Request DTO for assigning a role to a user
 */
@Builder
public record UserRoleAssignmentRequest(
        @NotNull(message = "User ID is required")
        Long userId,
        
        @NotNull(message = "Role ID is required")
        Long roleId,
        
        @Size(max = 500, message = "Assignment reason cannot exceed 500 characters")
        String assignmentReason,
        
        Boolean inheritPermissions,
        
        LocalDateTime validFrom,
        
        @Future(message = "Valid-to date must be in the future")
        LocalDateTime validTo,
        
        Long assignedById,
        
        UserRoleStatus status,
        
        @Size(max = 1000, message = "Restrictions cannot exceed 1000 characters")
        String restrictions
) {
    /**
     * Convert request DTO to entity
     * Note: user and role relationships need to be set separately
     */
    public UserRole toEntity() {
        LocalDateTime now = LocalDateTime.now();
        return UserRole.builder()
                .assignmentReason(assignmentReason)
                .inheritPermissions(inheritPermissions != null ? inheritPermissions : true)
                .active(true)
                .assignedAt(now)
                .validFrom(validFrom != null ? validFrom : now)
                .validTo(validTo)
                .status(status != null ? status : UserRoleStatus.PENDING)
                .restrictions(restrictions)
                .build();
    }
}
