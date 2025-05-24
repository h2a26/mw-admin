package org.hein.service;

import org.hein.api.request.userrole.UserRoleAssignmentRequest;
import org.hein.api.response.userrole.UserRoleResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user role assignments with validity periods and approval workflow
 */
public interface UserRoleService {

    /**
     * Assign a role to a user
     */
    UserRoleResponse assignRole(UserRoleAssignmentRequest request);
    
    /**
     * Remove a role from a user
     */
    void removeRole(Long userId, Long roleId);
    
    /**
     * Get all role assignments for a user
     */
    List<UserRoleResponse> findByUserId(Long userId);
    
    /**
     * Get all users assigned to a role
     */
    List<UserRoleResponse> findByRoleId(Long roleId);
    
    /**
     * Approve a role assignment
     */
    UserRoleResponse approveAssignment(Long userRoleId, String approverNotes);
    
    /**
     * Reject a role assignment
     */
    UserRoleResponse rejectAssignment(Long userRoleId, String rejectionReason);
    
    /**
     * Find role assignments that require approval
     */
    List<UserRoleResponse> findPendingApprovals();
    
    /**
     * Find role assignments that will expire within a specified number of days
     */
    List<UserRoleResponse> findExpiringAssignments(Integer days);
    
    /**
     * Extend the validity period of a role assignment
     */
    UserRoleResponse extendValidity(Long userRoleId, LocalDateTime newExpiryDate);
    
    /**
     * Revoke a role assignment immediately (before expiry date)
     */
    void revokeAssignment(Long userRoleId, String revocationReason);
}
