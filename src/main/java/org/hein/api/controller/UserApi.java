package org.hein.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hein.api.request.user.UserCreateRequest;
import org.hein.api.request.user.UserRequest;
import org.hein.api.response.user.UserResponse;
import org.hein.service.UserService;
import org.hein.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for managing users with integrated role-based access control
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "APIs for managing users, including role assignments")
public class UserApi {

    private final UserService userService;

    public UserApi(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     */
    @PostMapping
    @Operation(summary = "Create a new user")
    @PreAuthorize("hasAuthority('users:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.create(request);
        return ApiResponse.of(response, HttpStatus.CREATED, null);
    }

    /**
     * Update an existing user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user")
    @PreAuthorize("hasAuthority('users:UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.update(id, request);
        return ApiResponse.of(response);
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    @PreAuthorize("hasAuthority('users:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ApiResponse.of(null, HttpStatus.NO_CONTENT);
    }

    /**
     * Get a user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID")
    @PreAuthorize("hasAuthority('users:VIEW')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        UserResponse response = userService.getById(id);
        return ApiResponse.of(response);
    }

    /**
     * Get all users
     */
    @GetMapping
    @Operation(summary = "Get all users")
    @PreAuthorize("hasAuthority('users:VIEW')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        List<UserResponse> users = userService.getAll();
        return ApiResponse.of(users);
    }
    
    /**
     * Assign roles to a user
     */
    @PostMapping("/{id}/roles")
    @Operation(summary = "Assign roles to a user")
    @PreAuthorize("hasAuthority('users:ASSIGN_ROLE')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable Long id, @RequestBody Set<Long> roleIds) {
        UserResponse response = userService.assignRoles(id, roleIds);
        return ApiResponse.of(response);
    }
    
    /**
     * Remove roles from a user
     */
    @DeleteMapping("/{id}/roles")
    @Operation(summary = "Remove roles from a user")
    @PreAuthorize("hasAuthority('users:REMOVE_ROLE')")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoles(
            @PathVariable Long id, @RequestBody Set<Long> roleIds) {
        UserResponse response = userService.removeRoles(id, roleIds);
        return ApiResponse.of(response);
    }
    
    /**
     * Assign direct permissions to a user
     */
    @PostMapping("/{id}/permissions")
    @Operation(summary = "Assign direct permissions to a user")
    @PreAuthorize("hasAuthority('users:ASSIGN_PERMISSION')")
    public ResponseEntity<ApiResponse<UserResponse>> assignDirectPermissions(
            @PathVariable Long id, @RequestBody Set<Long> permissionIds) {
        UserResponse response = userService.assignDirectPermissions(id, permissionIds);
        return ApiResponse.of(response);
    }
    
    /**
     * Remove direct permissions from a user
     */
    @DeleteMapping("/{id}/permissions")
    @Operation(summary = "Remove direct permissions from a user")
    @PreAuthorize("hasAuthority('users:REMOVE_PERMISSION')")
    public ResponseEntity<ApiResponse<UserResponse>> removeDirectPermissions(
            @PathVariable Long id, @RequestBody Set<Long> permissionIds) {
        UserResponse response = userService.removeDirectPermissions(id, permissionIds);
        return ApiResponse.of(response);
    }
    
    /**
     * Enable or disable a user account
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "Enable or disable a user account")
    @PreAuthorize("hasAuthority('users:UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> setEnabled(
            @PathVariable Long id, @RequestParam boolean enabled) {
        UserResponse response = userService.setEnabled(id, enabled);
        return ApiResponse.of(response);
    }
    
    /**
     * Lock or unlock a user account
     */
    @PutMapping("/{id}/lock")
    @Operation(summary = "Lock or unlock a user account")
    @PreAuthorize("hasAuthority('users:UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> setLocked(
            @PathVariable Long id, @RequestParam boolean locked) {
        UserResponse response = userService.setLocked(id, locked);
        return ApiResponse.of(response);
    }
    
    /**
     * Reset a user's password (admin function)
     */
    @PutMapping("/{id}/reset-password")
    @Operation(summary = "Reset a user's password")
    @PreAuthorize("hasAuthority('users:RESET_PASSWORD')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ApiResponse.of(null, HttpStatus.NO_CONTENT, null);
    }
    
    /**
     * Find users by role
     */
    @GetMapping("/by-role/{roleId}")
    @Operation(summary = "Find users with a specific role")
    @PreAuthorize("hasAuthority('users:VIEW') or hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findByRoleId(@PathVariable Long roleId) {
        List<UserResponse> users = userService.findByRoleId(roleId);
        return ApiResponse.of(users);
    }
    
    /**
     * Find users with roles expiring soon
     */
    @GetMapping("/expiring-roles")
    @Operation(summary = "Find users with roles expiring within a specific timeframe")
    @PreAuthorize("hasAuthority('users:VIEW') or hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findByRoleExpiringInDays(
            @RequestParam(defaultValue = "30") int days) {
        List<UserResponse> users = userService.findByRoleExpiringInDays(days);
        return ApiResponse.of(users);
    }
}
