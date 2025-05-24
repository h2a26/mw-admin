package org.hein.service;

import org.hein.api.request.feature.FeatureCreateRequest;
import org.hein.api.response.feature.FeatureResponse;

import java.util.List;

/**
 * Service for managing features, supporting hierarchical structure and feature operations
 */
public interface FeatureService {
    
    /**
     * Create a new feature
     */
    FeatureResponse create(FeatureCreateRequest request);
    
    /**
     * Update an existing feature
     */
    FeatureResponse update(Long id, FeatureCreateRequest request);
    
    /**
     * Delete a feature by ID
     */
    void deleteById(Long id);
    
    /**
     * Toggle a feature's enabled status
     */
    FeatureResponse toggleStatus(Long id);
    
    /**
     * Find a feature by ID
     * @param id the feature ID
     * @param includeChildren whether to include child features in the response
     */
    FeatureResponse findById(Long id, boolean includeChildren);
    
    /**
     * Find all features
     * @param topLevelOnly whether to return only top-level features (no parent)
     * @param includeChildren whether to include child features in the response
     */
    List<FeatureResponse> findAll(boolean topLevelOnly, boolean includeChildren);
    
    /**
     * Find children of a specific feature
     */
    List<FeatureResponse> findChildren(Long id);
    
    /**
     * Move a feature to be a child of another feature
     */
    FeatureResponse moveToParent(Long id, Long parentId);
    
    /**
     * Remove parent relationship, making this a top-level feature
     */
    FeatureResponse removeParent(Long id);
}
