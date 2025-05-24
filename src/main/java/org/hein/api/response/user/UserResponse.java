package org.hein.api.response.user;

import org.hein.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean locked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Set<Long> roleIds
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.isLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getId())
                        .collect(Collectors.toSet())
        );
    }
}