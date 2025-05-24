package org.hein.service;

import org.hein.api.request.user.UserCreateRequest;
import org.hein.api.request.user.UserRequest;
import org.hein.api.response.user.UserResponse;
import org.hein.entity.User;

import java.util.List;
import java.util.Set;

/**
 * Service for managing users with integrated role-based access control
 */
public interface UserService {

    /**
     * Create a new user with roles and permissions
     */
    UserResponse create(UserCreateRequest request);
    
    /**
     * Update an existing user
     */
    UserResponse update(Long id, UserRequest request);
    
    /**
     * Get a user by ID with roles
     */
    UserResponse getById(Long id);
    
    /**
     * Get all users with basic information
     */
    List<UserResponse> getAll();
    
    /**
     * Delete a user by ID
     */
    void deleteById(Long id);
    
    /**
     * Find a user by username
     */
    User findByUsername(String username);
    
    /**
     * Find a user by email
     */
    User findByEmail(String email);
    
    /**
     * Assign roles to a user
     */
    UserResponse assignRoles(Long userId, Set<Long> roleIds);
    
    /**
     * Remove roles from a user
     */
    UserResponse removeRoles(Long userId, Set<Long> roleIds);
    
    /**
     * Assign direct permissions to a user (outside of roles)
     */
    UserResponse assignDirectPermissions(Long userId, Set<Long> permissionIds);
    
    /**
     * Remove direct permissions from a user
     */
    UserResponse removeDirectPermissions(Long userId, Set<Long> permissionIds);
    
    /**
     * Change a user's password
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
    
    /**
     * Reset a user's password (admin function)
     */
    void resetPassword(Long userId, String newPassword);
    
    /**
     * Enable or disable a user account
     */
    UserResponse setEnabled(Long userId, boolean enabled);
    
    /**
     * Lock or unlock a user account
     */
    UserResponse setLocked(Long userId, boolean locked);
    
    /**
     * Find users with a specific role
     */
    List<UserResponse> findByRoleId(Long roleId);
    
    /**
     * Find users with roles expiring within a specific timeframe
     */
    List<UserResponse> findByRoleExpiringInDays(int days);
}
