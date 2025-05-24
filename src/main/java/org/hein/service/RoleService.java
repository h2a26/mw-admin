package org.hein.service;

import org.hein.api.request.role.RoleCreateRequest;
import org.hein.api.response.role.RoleResponse;

import java.util.List;
import java.util.Set;

/**
 * Service for managing roles with hierarchical capabilities
 */
public interface RoleService {

    /**
     * Create a new role
     */
    RoleResponse create(RoleCreateRequest request);

    /**
     * Update an existing role
     */
    RoleResponse update(Long id, RoleCreateRequest request);

    /**
     * Delete a role by ID
     */
    void deleteById(Long id);

    /**
     * Find a role by ID
     * @param includePermissions whether to include permissions in the response
     * @param includeChildRoles whether to include child roles in the response
     */
    RoleResponse findById(Long id, boolean includePermissions, boolean includeChildRoles);

    /**
     * Find all roles
     * @param topLevelOnly whether to return only top-level roles (no parent)
     * @param includePermissions whether to include permissions in the response
     * @param includeChildRoles whether to include child roles in the response
     */
    List<RoleResponse> findAll(boolean topLevelOnly, boolean includePermissions, boolean includeChildRoles);

    /**
     * Find child roles for a specific role
     */
    List<RoleResponse> findChildRoles(Long id);

    /**
     * Move a role to be a child of another role
     */
    RoleResponse moveToParent(Long id, Long parentId);

    /**
     * Remove parent relationship, making this a top-level role
     */
    RoleResponse removeParent(Long id);

    /**
     * Add permissions to a role
     */
    RoleResponse addPermissions(Long id, Set<Long> permissionIds);

    /**
     * Remove permissions from a role
     */
    RoleResponse removePermissions(Long id, Set<Long> permissionIds);

    /**
     * Toggle role enabled status
     */
    RoleResponse toggleStatus(Long id);

    /**
     * Find roles with expiration dates in the given date range
     * @param days number of days from now
     */
    List<RoleResponse> findExpiringRoles(Integer days);
}
