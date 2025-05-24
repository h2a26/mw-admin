package org.hein.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hein.api.request.permission.PermissionCreateRequest;
import org.hein.api.response.permission.PermissionResponse;
import org.hein.service.PermissionService;
import org.hein.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Permission resources
 */
@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permission Management", description = "APIs for managing permissions")
public class PermissionApi {

    private final PermissionService permissionService;

    public PermissionApi(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Create a new permission
     */
    @PostMapping
    @Operation(summary = "Create a new permission")
    @PreAuthorize("hasAuthority('permissions:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<PermissionResponse>> create(
            @Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse response = permissionService.create(request);
        return ApiResponse.of(response, HttpStatus.CREATED, null);
    }

    /**
     * Update an existing permission
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing permission")
    @PreAuthorize("hasAuthority('permissions:UPDATE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse response = permissionService.update(id, request);
        return ApiResponse.of(response);
    }

    /**
     * Delete a permission by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a permission")
    @PreAuthorize("hasAuthority('permissions:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissionService.deleteById(id);
        return ApiResponse.of(null, HttpStatus.NO_CONTENT, null);
    }

    /**
     * Get a permission by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a permission by ID")
    @PreAuthorize("hasAuthority('permissions:VIEW')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(@PathVariable Long id) {
        PermissionResponse response = permissionService.findById(id);
        return ApiResponse.of(response);
    }

    /**
     * Get all permissions
     */
    @GetMapping
    @Operation(summary = "Get all permissions")
    @PreAuthorize("hasAuthority('permissions:VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAll(
            @Parameter(description = "Filter permissions by feature ID")
            @RequestParam(required = false) Long featureId) {
        List<PermissionResponse> permissions = featureId != null ? 
                permissionService.findByFeatureId(featureId) : 
                permissionService.findAll();
        return ApiResponse.of(permissions);
    }
    
    /**
     * Get permissions that require approval
     */
    @GetMapping("/requires-approval")
    @Operation(summary = "Get permissions that require approval")
    @PreAuthorize("hasAuthority('permissions:VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRequiresApproval() {
        List<PermissionResponse> permissions = permissionService.findRequiresApproval();
        return ApiResponse.of(permissions);
    }
}
