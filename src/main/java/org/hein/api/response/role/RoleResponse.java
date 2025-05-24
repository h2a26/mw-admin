package org.hein.api.response.role;

import org.hein.api.response.permission.PermissionResponse;
import org.hein.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Response DTO for Role entity with hierarchical structure support
 */
public record RoleResponse(
        Long id,
        String name,
        String code,
        String description,
        Integer priority,
        boolean systemRole,
        boolean defaultRole,
        boolean active,
        Long parentId,
        String parentName,
        List<RoleResponse> childRoles,
        LocalDateTime expiryDate,
        Set<PermissionResponse> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Convert Role entity to response DTO without permissions or children
     */
    public static RoleResponse fromEntity(Role role) {
        return fromEntity(role, false, false);
    }
    
    /**
     * Convert Role entity to response DTO
     * @param role The role entity to convert
     * @param includePermissions Whether to include permissions
     * @param includeChildren Whether to include child roles
     */
    public static RoleResponse fromEntity(Role role, boolean includePermissions, boolean includeChildren) {
        if (role == null) {
            return null;
        }
        
        Role parent = role.getParent();
        List<RoleResponse> childRoleResponses = null;
        Set<PermissionResponse> permissionResponses = null;
        
        // Include child roles if requested
        if (includeChildren && role.getChildRoles() != null && !role.getChildRoles().isEmpty()) {
            childRoleResponses = role.getChildRoles().stream()
                    .map(child -> fromEntity(child, false, false)) // Avoid infinite recursion
                    .collect(Collectors.toList());
        }
        
        // Include permissions if requested
        if (includePermissions && role.getPermissions() != null) {
            permissionResponses = role.getPermissions().stream()
                    .map(PermissionResponse::fromEntity)
                    .collect(Collectors.toSet());
        }
        
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                role.getPriority(),
                role.isSystemRole(),
                role.isDefaultRole(),
                role.isActive(),
                parent != null ? parent.getId() : null,
                parent != null ? parent.getName() : null,
                childRoleResponses,
                role.getExpiryDate(),
                permissionResponses,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}