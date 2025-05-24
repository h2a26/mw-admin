package org.hein.api.response.feature;

import org.hein.entity.Feature;

public record FeatureResponse(
        Long id,
        String name,
        String description
) {
    public static FeatureResponse from(Feature feature) {
        return new FeatureResponse(
                feature.getId(),
                feature.getName(),
                feature.getDescription()
        );
    }
}
