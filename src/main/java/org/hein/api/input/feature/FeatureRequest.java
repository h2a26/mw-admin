package org.hein.api.input.feature;

import jakarta.validation.constraints.NotBlank;

public record FeatureRequest(
        @NotBlank(message = "Feature name is required.")
        String name,

        String description
) { }
