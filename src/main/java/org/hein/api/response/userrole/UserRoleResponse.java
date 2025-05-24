package org.hein.api.response.userrole;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hein.api.response.role.RoleResponse;
import org.hein.api.response.user.UserSummaryResponse;
import org.hein.entity.UserRole;
import org.hein.entity.UserRoleStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for UserRole entities with all the details needed for the frontend
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRoleResponse(
    Long id,
    UserSummaryResponse user,
    RoleResponse role,
    UserRoleStatus status,
    boolean active,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime assignedAt,
    
    UserSummaryResponse assignedBy,
    String assignmentReason,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime validFrom,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime validTo,
    
    UserSummaryResponse approvedBy,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime approvedAt,
    
    String approverNotes,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime rejectionDate,
    
    String rejectionReason,
    
    UserSummaryResponse revokedBy,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime revocationDate,
    
    String revocationReason,
    
    boolean inheritPermissions,
    String restrictions
) {
    /**
     * Create a UserRoleResponse from a UserRole entity
     */
    public static UserRoleResponse fromEntity(UserRole userRole) {
        if (userRole == null) {
            return null;
        }
        
        return new UserRoleResponse(
            userRole.getId(),
            userRole.getUser() != null ? UserSummaryResponse.fromEntity(userRole.getUser()) : null,
            userRole.getRole() != null ? RoleResponse.fromEntity(userRole.getRole(), true, false) : null,
            userRole.getStatus(),
            userRole.isActive(),
            userRole.getAssignedAt(),
            userRole.getAssignedBy() != null ? UserSummaryResponse.fromEntity(userRole.getAssignedBy()) : null,
            userRole.getAssignmentReason(),
            userRole.getValidFrom(),
            userRole.getValidTo(),
            userRole.getApprovedBy() != null ? UserSummaryResponse.fromEntity(userRole.getApprovedBy()) : null,
            userRole.getApprovedAt(),
            userRole.getApproverNotes(),
            userRole.getRejectionDate(),
            userRole.getRejectionReason(),
            userRole.getRevokedBy() != null ? UserSummaryResponse.fromEntity(userRole.getRevokedBy()) : null,
            userRole.getRevocationDate(),
            userRole.getRevocationReason(),
            userRole.isInheritPermissions(),
            userRole.getRestrictions()
        );
    }
}
