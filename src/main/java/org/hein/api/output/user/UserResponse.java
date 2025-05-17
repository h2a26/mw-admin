package org.hein.api.output.user;

import org.hein.entity.User;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String username,
        String email,
        Set<Long> roleIds
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getId())
                        .collect(Collectors.toSet())
        );
    }
}
