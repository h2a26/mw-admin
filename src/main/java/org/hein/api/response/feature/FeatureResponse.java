package org.hein.api.response.feature;

import lombok.*;
import org.hein.entity.Feature;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for Feature entity with hierarchical structure support
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private boolean enabled;
    private Integer displayOrder;
    private String icon;
    private Long parentId;
    private String parentName;
    private List<FeatureResponse> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeatureResponse fromEntity(Feature feature, boolean includeChildren) {
        if (feature == null) {
            return null;
        }

        Feature parent = feature.getParent();
        List<FeatureResponse> childResponses = null;

        if (includeChildren && feature.getChildren() != null && !feature.getChildren().isEmpty()) {
            childResponses = feature.getChildren().stream()
                    .map(child -> fromEntity(child, false))
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
