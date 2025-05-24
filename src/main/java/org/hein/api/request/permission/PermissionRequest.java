package org.hein.api.request.permission;

import jakarta.validation.constraints.NotNull;

public record PermissionRequest(
        @NotNull(message = "Feature ID is required.")
        Long featureId,
        @NotNull(message = "Action is required.")
        String action
) {}
