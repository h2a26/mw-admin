package org.hein.service;

import org.hein.api.request.permission.PermissionCreateRequest;
import org.hein.api.response.permission.PermissionResponse;

import java.util.List;

/**
 * Service for managing permissions with feature relationships and approval workflows
 */
public interface PermissionService {
    
    /**
     * Create a new permission
     */
    PermissionResponse create(PermissionCreateRequest request);
    
    /**
     * Update an existing permission
     */
    PermissionResponse update(Long id, PermissionCreateRequest request);
    
    /**
     * Delete a permission by ID
     */
    void deleteById(Long id);
    
    /**
     * Find a permission by ID
     */
    PermissionResponse findById(Long id);
    
    /**
     * Find all permissions
     */
    List<PermissionResponse> findAll();
    
    /**
     * Find permissions by feature ID
     */
    List<PermissionResponse> findByFeatureId(Long featureId);
    
    /**
     * Find permissions that require approval
     */
    List<PermissionResponse> findRequiresApproval();
}

