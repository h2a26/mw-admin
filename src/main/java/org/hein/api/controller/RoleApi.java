package org.hein.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hein.api.request.role.RoleCreateRequest;
import org.hein.api.response.role.RoleResponse;
import org.hein.service.RoleService;
import org.hein.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for managing Role resources
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleApi {

    private final RoleService roleService;

    public RoleApi(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Create a new role
     */
    @PostMapping
    @Operation(summary = "Create a new role")
    @PreAuthorize("hasAuthority('roles:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody RoleCreateRequest request) {
        RoleResponse response = roleService.create(request);
        return ApiResponse.of(response, HttpStatus.CREATED, null);
    }

    /**
     * Update an existing role
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing role")
    @PreAuthorize("hasAuthority('roles:UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleCreateRequest request) {
        RoleResponse response = roleService.update(id, request);
        return ApiResponse.of(response);
    }

    /**
     * Delete a role by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role")
    @PreAuthorize("hasAuthority('roles:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.deleteById(id);
        return ApiResponse.of(null, HttpStatus.NO_CONTENT, null);
    }

    /**
     * Get a role by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a role by ID")
    @PreAuthorize("hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<RoleResponse>> getById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includePermissions,
            @RequestParam(defaultValue = "false") boolean includeChildRoles) {
        RoleResponse response = roleService.findById(id, includePermissions, includeChildRoles);
        return ApiResponse.of(response);
    }

    /**
     * Get all roles
     */
    @GetMapping
    @Operation(summary = "Get all roles")
    @PreAuthorize("hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAll(
            @RequestParam(defaultValue = "false") boolean topLevelOnly,
            @RequestParam(defaultValue = "true") boolean includePermissions,
            @RequestParam(defaultValue = "false") boolean includeChildRoles) {
        List<RoleResponse> roles = roleService.findAll(topLevelOnly, includePermissions, includeChildRoles);
        return ApiResponse.of(roles);
    }
    
    /**
     * Get child roles for a specific role
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "Get child roles for a specific role")
    @PreAuthorize("hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getChildRoles(@PathVariable Long id) {
        List<RoleResponse> childRoles = roleService.findChildRoles(id);
        return ApiResponse.of(childRoles);
    }
    
    /**
     * Move a role to be a child of another role
     */
    @PutMapping("/{id}/parent/{parentId}")
    @Operation(summary = "Move a role to be a child of another role")
    @PreAuthorize("hasAuthority('roles:UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> moveToParent(
            @PathVariable Long id, @PathVariable Long parentId) {
        RoleResponse response = roleService.moveToParent(id, parentId);
        return ApiResponse.of(response);
    }
    
    /**
     * Remove parent relationship, making this a top-level role
     */
    @DeleteMapping("/{id}/parent")
    @Operation(summary = "Remove parent relationship")
    @PreAuthorize("hasAuthority('roles:UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> removeParent(@PathVariable Long id) {
        RoleResponse response = roleService.removeParent(id);
        return ApiResponse.of(response);
    }
    
    /**
     * Add permissions to a role
     */
    @PostMapping("/{id}/permissions")
    @Operation(summary = "Add permissions to a role")
    @PreAuthorize("hasAuthority('roles:UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> addPermissions(
            @PathVariable Long id, @RequestBody Set<Long> permissionIds) {
        RoleResponse response = roleService.addPermissions(id, permissionIds);
        return ApiResponse.of(response);
    }
    
    /**
     * Remove permissions from a role
     */
    @DeleteMapping("/{id}/permissions")
    @Operation(summary = "Remove permissions from a role")
    @PreAuthorize("hasAuthority('roles:UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermissions(
            @PathVariable Long id, @RequestBody Set<Long> permissionIds) {
        RoleResponse response = roleService.removePermissions(id, permissionIds);
        return ApiResponse.of(response);
    }
    
    /**
     * Toggle role enabled status
     */
    @PutMapping("/{id}/toggle")
    @Operation(summary = "Toggle role enabled status")
    @PreAuthorize("hasAuthority('roles:UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> toggleStatus(@PathVariable Long id) {
        RoleResponse response = roleService.toggleStatus(id);
        return ApiResponse.of(response);
    }
    
    /**
     * Find roles that will expire within a specified number of days
     */
    @GetMapping("/expiring")
    @Operation(summary = "Find roles expiring within the specified days")
    @PreAuthorize("hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> findExpiringRoles(
            @RequestParam(defaultValue = "30") Integer days) {
        List<RoleResponse> roles = roleService.findExpiringRoles(days);
        return ApiResponse.of(roles);
    }
}
