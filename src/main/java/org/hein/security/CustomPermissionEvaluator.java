package org.hein.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.entity.Feature;
import org.hein.exceptions.PermissionDeniedException;
import org.hein.repository.FeatureRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator for RBAC that supports both
 * feature-action based permissions and direct permission strings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final FeatureRepository featureRepository;
    // We'll keep the repository reference for feature lookups
    // Additional services can be uncommented when needed for more complex permission checks

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Handle Feature-specific permissions
        if (targetDomainObject instanceof Feature feature) {
            String permissionString = permission.toString();
            return checkPermissionOnFeature(authentication, feature, permissionString);
        }
        
        // Check for direct permission strings (e.g., 'users:CREATE')
        String permissionString = permission.toString();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(permissionString));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Handle Feature-specific permissions
        if (targetType.equalsIgnoreCase("Feature")) {
            Feature feature = getFeatureById(targetId);
            String permissionString = permission.toString();
            return checkPermissionOnFeature(authentication, feature, permissionString);
        }
        
        // Handle Entity-specific permissions (e.g., can access a specific User entity)
        if (targetType.equalsIgnoreCase("Entity")) {
            // Format: entityType:action, e.g., "user:view"
            String permissionString = permission.toString();
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals(permissionString));
        }

        return false;
    }

    /**
     * Check if the authenticated user has the specified permission on a feature.
     * 
     * @param authentication The Spring Security Authentication object
     * @param feature The feature to check permission for
     * @param permissionString The permission string to check (e.g., "VIEW", "EDIT")
     * @return true if the user has the requested permission, false otherwise
     */
    private boolean checkPermissionOnFeature(Authentication authentication, Feature feature, String permissionString) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Build the full permission string in the format "feature:action"
        String fullPermission = feature.getName().toLowerCase() + ":" + permissionString.toUpperCase();
        
        // Check if the user has this permission
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(fullPermission));
    }

    /**
     * Get a Feature entity by its ID.
     * 
     * @param id The ID of the feature to retrieve
     * @return The Feature entity
     * @throws PermissionDeniedException if the feature doesn't exist or the ID is invalid
     */
    private Feature getFeatureById(Serializable id) {
        if (id == null) {
            throw new PermissionDeniedException("Feature ID cannot be null");
        }

        try {
            Long featureId = Long.parseLong(id.toString());
            return featureRepository.findById(featureId)
                    .orElseThrow(() -> new PermissionDeniedException(
                        "Feature not found",
                        String.valueOf(featureId),
                        null,
                        null
                    ));
        } catch (NumberFormatException e) {
            throw new PermissionDeniedException(
                "Invalid feature ID format",
                id.toString(),
                null,
                null
            );
        }
    }
}
