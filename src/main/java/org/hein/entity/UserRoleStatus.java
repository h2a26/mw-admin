package org.hein.entity;

/**
 * Enumeration of possible statuses for user role assignments
 * This supports the approval workflow and lifecycle management of role assignments
 */
public enum UserRoleStatus {
    /**
     * Role is pending approval
     */
    PENDING,
    
    /**
     * Role assignment is active and valid
     */
    ACTIVE,
    
    /**
     * Role assignment was rejected during approval
     */
    REJECTED,
    
    /**
     * Role assignment was revoked before expiry
     */
    REVOKED,
    
    /**
     * Role assignment has expired (system-set)
     */
    EXPIRED
}
