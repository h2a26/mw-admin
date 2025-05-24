package org.hein.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hein.api.request.userrole.UserRoleAssignmentRequest;
import org.hein.api.response.userrole.UserRoleResponse;
import org.hein.service.UserRoleService;
import org.hein.utils.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing user role assignments with validity periods and approval workflow
 */
@RestController
@RequestMapping("/api/v1/user-roles")
@Tag(name = "User Role Management", description = "APIs for managing user role assignments")
public class UserRoleApi {

    private final UserRoleService userRoleService;

    public UserRoleApi(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * Assign a role to a user
     */
    @PostMapping
    @Operation(summary = "Assign a role to a user")
    @PreAuthorize("hasAuthority('users:ASSIGN_ROLE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<UserRoleResponse>> assignRole(
            @Valid @RequestBody UserRoleAssignmentRequest request) {
        UserRoleResponse response = userRoleService.assignRole(request);
        return ApiResponse.of(response, HttpStatus.CREATED, null);
    }

    /**
     * Remove a role from a user
     */
    @DeleteMapping("/{userId}/{roleId}")
    @Operation(summary = "Remove a role from a user")
    @PreAuthorize("hasAuthority('users:REMOVE_ROLE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @PathVariable Long userId, @PathVariable Long roleId) {
        userRoleService.removeRole(userId, roleId);
        return ApiResponse.of(null, HttpStatus.NO_CONTENT, null);
    }

    /**
     * Get all role assignments for a user
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all role assignments for a user")
    @PreAuthorize("hasAuthority('users:VIEW') or hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getByUserId(@PathVariable Long userId) {
        List<UserRoleResponse> responses = userRoleService.findByUserId(userId);
        return ApiResponse.of(responses);
    }

    /**
     * Get all users assigned to a role
     */
    @GetMapping("/role/{roleId}")
    @Operation(summary = "Get all users assigned to a role")
    @PreAuthorize("hasAuthority('users:VIEW') or hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getByRoleId(@PathVariable Long roleId) {
        List<UserRoleResponse> responses = userRoleService.findByRoleId(roleId);
        return ApiResponse.of(responses);
    }

    /**
     * Approve a role assignment
     */
    @PutMapping("/{userRoleId}/approve")
    @Operation(summary = "Approve a role assignment")
    @PreAuthorize("hasAuthority('users:APPROVE_ROLE')")
    public ResponseEntity<ApiResponse<UserRoleResponse>> approveAssignment(
            @PathVariable Long userRoleId,
            @RequestParam(required = false) String approverNotes) {
        UserRoleResponse response = userRoleService.approveAssignment(userRoleId, approverNotes);
        return ApiResponse.of(response);
    }

    /**
     * Reject a role assignment
     */
    @PutMapping("/{userRoleId}/reject")
    @Operation(summary = "Reject a role assignment")
    @PreAuthorize("hasAuthority('users:APPROVE_ROLE')")
    public ResponseEntity<ApiResponse<UserRoleResponse>> rejectAssignment(
            @PathVariable Long userRoleId,
            @RequestParam String rejectionReason) {
        UserRoleResponse response = userRoleService.rejectAssignment(userRoleId, rejectionReason);
        return ApiResponse.of(response);
    }

    /**
     * Find role assignments that require approval
     */
    @GetMapping("/pending")
    @Operation(summary = "Find role assignments that require approval")
    @PreAuthorize("hasAuthority('users:APPROVE_ROLE')")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> findPendingApprovals() {
        List<UserRoleResponse> responses = userRoleService.findPendingApprovals();
        return ApiResponse.of(responses);
    }

    /**
     * Find role assignments that will expire within a specified number of days
     */
    @GetMapping("/expiring")
    @Operation(summary = "Find role assignments that will expire within a specified number of days")
    @PreAuthorize("hasAuthority('users:VIEW') or hasAuthority('roles:VIEW')")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> findExpiringAssignments(
            @RequestParam(defaultValue = "30") Integer days) {
        List<UserRoleResponse> responses = userRoleService.findExpiringAssignments(days);
        return ApiResponse.of(responses);
    }

    /**
     * Extend the validity period of a role assignment
     */
    @PutMapping("/{userRoleId}/extend")
    @Operation(summary = "Extend the validity period of a role assignment")
    @PreAuthorize("hasAuthority('users:ASSIGN_ROLE')")
    public ResponseEntity<ApiResponse<UserRoleResponse>> extendValidity(
            @PathVariable Long userRoleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newExpiryDate) {
        UserRoleResponse response = userRoleService.extendValidity(userRoleId, newExpiryDate);
        return ApiResponse.of(response);
    }

    /**
     * Revoke a role assignment immediately (before expiry date)
     */
    @PutMapping("/{userRoleId}/revoke")
    @Operation(summary = "Revoke a role assignment immediately")
    @PreAuthorize("hasAuthority('users:REMOVE_ROLE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> revokeAssignment(
            @PathVariable Long userRoleId,
            @RequestParam String revocationReason) {
        userRoleService.revokeAssignment(userRoleId, revocationReason);
        return ApiResponse.of(null, HttpStatus.NO_CONTENT, null);
    }
}
