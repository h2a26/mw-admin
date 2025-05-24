package org.hein.api.response.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hein.entity.User;

/**
 * A simplified response DTO for User entities
 * Used when we need to include basic user information in other responses
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserSummaryResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String mobilePhone,
    boolean enabled,
    boolean locked
) {
    /**
     * Create a UserSummaryResponse from a User entity
     */
    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserSummaryResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getMobilePhone(),
            user.isEnabled(),
            user.isLocked()
        );
    }
}
