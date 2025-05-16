package org.hein.api.output.auth;

import org.hein.entity.User;

public record UserRegistrationResponse (
     String username,
     String email) {

    public static UserRegistrationResponse from(User user) {
        return new UserRegistrationResponse(
                user.getUsername(),
                user.getEmail());
    }
}
