package org.hein.api.response.user;

import org.hein.api.response.permission.PermissionResponse;
import org.hein.api.response.role.RoleResponse;
import org.hein.entity.User;
import org.hein.entity.UserRole;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extended response DTO for User entity with detailed permissions information
 */
public record UserWithPermissionsResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean locked,
        boolean twoFactorEnabled,
        Set<RoleResponse> roles,
        Set<PermissionResponse> allPermissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Convert User entity to response DTO with detailed permissions
     */
    public static UserWithPermissionsResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        // Get all permissions (direct + role-based)
        Set<PermissionResponse> allPermissionResponses = user.getPermissions().stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toSet());
                
        return new UserWithPermissionsResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.isLocked(),
                user.isTwoFactorEnabled(),
                // Only include valid role assignments
                user.getUserRoles().stream()
                        .filter(UserRole::isValid)
                        .map(ur -> RoleResponse.fromEntity(ur.getRole(), true, false)) // Include role permissions
                        .collect(Collectors.toSet()),
                allPermissionResponses,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
