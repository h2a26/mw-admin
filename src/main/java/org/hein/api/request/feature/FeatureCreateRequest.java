package org.hein.api.request.feature;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hein.entity.Feature;

/**
 * Request DTO for creating or updating a Feature
 */
@Builder
public record FeatureCreateRequest(
        @NotBlank(message = "Feature name is required.")
        @Size(min = 2, max = 50, message = "Feature name must be between 2 and 50 characters")
        String name,
        
        @NotBlank(message = "Feature code is required.")
        @Pattern(regexp = "^[a-z0-9_]+$", message = "Feature code must contain only lowercase letters, numbers, and underscores")
        @Size(min = 2, max = 50, message = "Feature code must be between 2 and 50 characters")
        String code,
        
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,
        
        Boolean enabled,
        
        Integer displayOrder,
        
        String icon,
        
        Long parentId
) {
        /**
         * Convert request DTO to entity
         * Note: parent relationship needs to be set separately
         */
        public Feature toEntity() {
                return Feature.builder()
                        .name(name)
                        .code(code)
                        .description(description)
                        .enabled(enabled != null ? enabled : true)
                        .displayOrder(displayOrder)
                        .icon(icon)
                        .build();
        }
}
