package org.hein.api.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRequest(
        @NotBlank(message = "Username is required.")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
        String username,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, message = "Password must be at least 8 characters.")
        String password,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        String email,

        @NotBlank(message = "First name is required.")
        @Size(max = 100, message = "First name cannot exceed 100 characters.")
        String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(max = 100, message = "Last name cannot exceed 100 characters.")
        String lastName,

        @NotEmpty(message = "At least one role must be assigned.")
        Set<Long> roleIds
) {}