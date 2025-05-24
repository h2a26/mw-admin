package org.hein.api.response.user;

import org.hein.api.response.role.RoleResponse;
import org.hein.entity.User;
import org.hein.entity.UserRole;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Response DTO for User entity with roles
 */
public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String mobilePhone,
        boolean enabled,
        boolean locked,
        boolean systemAccount,
        boolean twoFactorEnabled,
        boolean passwordExpired,
        LocalDateTime lastLoginAt,
        LocalDateTime passwordChangedAt,
        Set<RoleResponse> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Convert User entity to response DTO with basic role information
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobilePhone(),
                user.isEnabled(),
                user.isLocked(),
                user.isSystemAccount(),
                user.isTwoFactorEnabled(),
                user.isPasswordExpired(),
                user.getLastLoginAt(),
                user.getPasswordChangedAt(),
                // Only include valid role assignments
                user.getUserRoles().stream()
                        .filter(UserRole::isValid)
                        .map(ur -> RoleResponse.fromEntity(ur.getRole()))
                        .collect(Collectors.toSet()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
