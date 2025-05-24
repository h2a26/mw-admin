package org.hein.api.request.feature;

import jakarta.validation.constraints.NotBlank;

public record FeatureRequest(
        @NotBlank(message = "Feature name is required.")
        String name,

        String description
) { }
