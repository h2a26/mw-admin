package org.hein.api.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleRequest(
        @NotBlank(message = "Role name is required.")
        String name,
        @Size(max = 255, message = "Description can be at most 255 characters.")
        String description,
        @NotEmpty(message = "At least one permission must be assigned.")
        Set<Long> permissionIds
) {}
