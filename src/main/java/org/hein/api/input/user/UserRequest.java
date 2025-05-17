package org.hein.api.input.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserRequest(
        @NotBlank(message = "Username is required.")
        String username,

        @NotBlank(message = "Password is required.")
        @Size(min = 6, message = "Password must be at least 6 characters.")
        String password,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        String email,

        @NotEmpty(message = "At least one role must be assigned.")
        Set<Long> roleIds
) {}
