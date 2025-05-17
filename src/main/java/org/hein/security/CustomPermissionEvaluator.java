package org.hein.security;

import lombok.RequiredArgsConstructor;
import org.hein.entity.Feature;
import org.hein.entity.User;
import org.hein.entity.Action;
import org.hein.exceptions.PermissionDeniedException;
import org.hein.repository.FeatureRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final FeatureRepository featureRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (targetDomainObject instanceof Feature feature) {
            Action action = Action.valueOf(permission.toString().toUpperCase());
            return checkFeaturePermission(authentication, feature, action);
        }

        // Fallback to simple permission check
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("PERMISSION_" + permission.toString()));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (targetType.equals("Feature")) {
            Feature feature = getFeatureById(targetId);
            Action action = Action.valueOf(permission.toString().toUpperCase());
            return checkFeaturePermission(authentication, feature, action);
        }

        return false;
    }

    private boolean checkFeaturePermission(Authentication authentication, Feature feature, Action action) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        User user = (User) authentication.getPrincipal();
        
        // Check if user has permission through any of their roles
        return user.getAllRoles().stream()
                .anyMatch(role -> role.hasPermission(feature, action));
    }

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
