package org.hein.api.response.feature;

import lombok.Builder;
import org.hein.entity.Feature;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for Feature entity with hierarchical structure support
 */
@Builder
public record FeatureResponse(
        Long id,
        String name,
        String code,
        String description,
        boolean enabled,
        Integer displayOrder,
        String icon,
        Long parentId,
        String parentName,
        List<FeatureResponse> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Convert Feature entity to response DTO without children
     */
    public static FeatureResponse from(Feature feature) {
        return fromEntity(feature, false);
    }
    
    /**
     * Convert Feature entity to response DTO
     * @param feature The feature entity to convert
     * @param includeChildren Whether to include child features
     */
    public static FeatureResponse fromEntity(Feature feature, boolean includeChildren) {
        if (feature == null) {
            return null;
        }
        
        Feature parent = feature.getParent();
        List<FeatureResponse> childResponses = null;
        
        if (includeChildren && feature.getChildren() != null && !feature.getChildren().isEmpty()) {
            childResponses = feature.getChildren().stream()
                    .map(child -> fromEntity(child, false)) // Avoid infinite recursion
                    .collect(Collectors.toList());
        }
        
        return FeatureResponse.builder()
                .id(feature.getId())
                .name(feature.getName())
                .code(feature.getCode())
                .description(feature.getDescription())
                .enabled(feature.isEnabled())
                .displayOrder(feature.getDisplayOrder())
                .icon(feature.getIcon())
                .parentId(parent != null ? parent.getId() : null)
                .parentName(parent != null ? parent.getName() : null)
                .children(childResponses)
                .createdAt(feature.getCreatedAt())
                .updatedAt(feature.getUpdatedAt())
                .build();
    }
}