package org.hein.api.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hein.entity.Role;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * Request DTO for creating or updating a Role
 */
@Builder
public record RoleCreateRequest(
        @NotBlank(message = "Role name is required")
        @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
        String name,
        
        @NotBlank(message = "Role code is required")
        @Pattern(regexp = "^[a-z0-9_]+$", message = "Role code must contain only lowercase letters, numbers, and underscores")
        @Size(min = 2, max = 50, message = "Role code must be between 2 and 50 characters")
        String code,
        
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,
        
        Integer priority,
        
        Boolean systemRole,
        
        Boolean defaultRole,
        
        Long parentId,
        
        LocalDateTime expiryDate,
        
        Set<Long> permissionIds
) {
    /**
     * Convert request DTO to entity
     * Note: parent relationship and permissions need to be set separately
     */
    public Role toEntity() {
        return Role.builder()
                .name(name)
                .code(code)
                .description(description)
                .priority(priority != null ? priority : 0)
                .systemRole(systemRole != null ? systemRole : false)
                .defaultRole(defaultRole != null ? defaultRole : false)
                .expiryDate(expiryDate)
                .build();
    }

    /**
     * Get the permission IDs, returning an empty set if null
     */
    public Set<Long> getPermissionIds() {
        return this.permissionIds != null ? this.permissionIds : Collections.emptySet();
    }
}