package org.hein.api.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
        String username,

        @Email(message = "Email must be valid.")
        String email,

        @Size(max = 100, message = "First name cannot exceed 100 characters.")
        String firstName,

        @Size(max = 100, message = "Last name cannot exceed 100 characters.")
        String lastName
) {}