package org.hein.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.entity.User;
import org.hein.repository.UserRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom PermissionEvaluator that integrates with our RBAC system
 * This allows us to use hasPermission() expressions in @PreAuthorize annotations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RbacPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository userRepository;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        if (permission == null) {
            log.warn("Permission check with null permission");
            return false;
        }
        
        return checkPermission(authentication, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        if (permission == null) {
            log.warn("Permission check with null permission");
            return false;
        }
        
        return checkPermission(authentication, permission.toString());
    }
    
    /**
     * Check if the authenticated user has the specified permission
     */
    private boolean checkPermission(Authentication authentication, String permissionName) {
        String username = authentication.getName();
        
        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            log.warn("Permission check for non-existent user: {}", username);
            return false;
        }
        
        // System accounts can have special privileges
        if (user.isSystemAccount()) {
            log.debug("System account access: {}", username);
            return true;
        }
        
        // Check if the user is enabled
        if (!user.isEnabled() || user.isLocked()) {
            log.debug("Permission denied for disabled/locked user: {}", username);
            return false;
        }
        
        // Check if the user has the permission directly or through roles
        boolean hasPermission = user.hasPermission(permissionName);
        
        if (hasPermission) {
            log.debug("User {} has permission: {}", username, permissionName);
        } else {
            log.debug("User {} denied permission: {}", username, permissionName);
        }
        
        return hasPermission;
    }
}
