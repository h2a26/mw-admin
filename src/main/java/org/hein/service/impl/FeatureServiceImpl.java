package org.hein.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.api.request.feature.FeatureCreateRequest;
import org.hein.api.response.feature.FeatureResponse;
import org.hein.entity.Feature;
import org.hein.repository.FeatureRepository;
import org.hein.service.FeatureService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = "features", allEntries = true)
    public FeatureResponse create(FeatureCreateRequest request) {
        Feature feature = new Feature();
        feature.setName(request.name());
        feature.setCode(request.code());
        feature.setDescription(request.description());
        feature.setDisplayOrder(request.displayOrder());
        feature.setIcon(request.icon());
        feature.setEnabled(request.enabled());
        
        // Set parent feature if provided
        if (request.parentId() != null) {
            Feature parent = featureRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent feature not found: " + request.parentId()));
            feature.setParent(parent);
        }
        
        Feature savedFeature = featureRepository.save(feature);
        return FeatureResponse.fromEntity(savedFeature, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", allEntries = true)
    public FeatureResponse update(Long id, FeatureCreateRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + id));
        
        feature.setName(request.name());
        feature.setCode(request.code());
        feature.setDescription(request.description());
        feature.setDisplayOrder(request.displayOrder());
        feature.setIcon(request.icon());
        feature.setEnabled(request.enabled());
        
        // Parent can't be the feature itself
        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new IllegalArgumentException("Feature cannot be its own parent");
        }
        
        // Update parent if provided and different from current
        if (request.parentId() != null) {
            Feature parent = featureRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent feature not found: " + request.parentId()));
            
            // Check for circular references
            if (isAncestor(feature, parent)) {
                throw new IllegalArgumentException("Circular reference detected in feature hierarchy");
            }
            
            feature.setParent(parent);
        } else if (request.parentId() == null && feature.getParent() != null) {
            // Remove parent if null was provided
            feature.setParent(null);
        }
        
        Feature updatedFeature = featureRepository.save(feature);
        return FeatureResponse.fromEntity(updatedFeature, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", allEntries = true)
    public void deleteById(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + id));
        
        // Check if feature has children
        if (!feature.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete feature with children. Remove children first.");
        }
        
        // Check if feature has permissions
        if (!feature.getPermissions().isEmpty()) {
            throw new IllegalStateException("Cannot delete feature with permissions. Remove permissions first.");
        }
        
        featureRepository.delete(feature);
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", allEntries = true)
    public FeatureResponse toggleStatus(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + id));
        
        feature.setEnabled(!feature.isEnabled());
        Feature updatedFeature = featureRepository.save(feature);
        
        return FeatureResponse.fromEntity(updatedFeature, false);
    }

    @Override
    @Cacheable(value = "features", key = "#id + '-' + #includeChildren")
    public FeatureResponse findById(Long id, boolean includeChildren) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + id));
        
        return FeatureResponse.fromEntity(feature, includeChildren);
    }

    @Override
    @Cacheable(value = "features", key = "'all-' + #topLevelOnly + '-' + #includeChildren")
    public List<FeatureResponse> findAll(boolean topLevelOnly, boolean includeChildren) {
        List<Feature> features;
        
        if (topLevelOnly) {
            features = featureRepository.findByParentIsNull();
        } else {
            features = featureRepository.findAll();
        }
        
        return features.stream()
                .map(feature -> FeatureResponse.fromEntity(feature, includeChildren))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "features", key = "'children-' + #id")
    public List<FeatureResponse> findChildren(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + id));
        
        return feature.getChildren().stream()
                .map(child -> FeatureResponse.fromEntity(child, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", allEntries = true)
    public FeatureResponse moveToParent(Long id, Long parentId) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + id));
        
        if (id.equals(parentId)) {
            throw new IllegalArgumentException("Feature cannot be its own parent");
        }
        
        Feature parent = featureRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent feature not found: " + parentId));
        
        // Check for circular references
        if (isAncestor(feature, parent)) {
            throw new IllegalArgumentException("Circular reference detected in feature hierarchy");
        }
        
        feature.setParent(parent);
        Feature updatedFeature = featureRepository.save(feature);
        
        return FeatureResponse.fromEntity(updatedFeature, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", allEntries = true)
    public FeatureResponse removeParent(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + id));
        
        if (feature.getParent() == null) {
            return FeatureResponse.fromEntity(feature, false);
        }
        
        feature.setParent(null);
        Feature updatedFeature = featureRepository.save(feature);
        
        return FeatureResponse.fromEntity(updatedFeature, false);
    }
    
    /**
     * Helper method to check if a potential parent is actually an ancestor of the feature
     * This prevents circular references in the hierarchy
     */
    private boolean isAncestor(Feature feature, Feature potentialAncestor) {
        if (feature == null || potentialAncestor == null) {
            return false;
        }
        
        List<Feature> ancestors = new ArrayList<>();
        Feature current = feature.getParent();
        
        while (current != null) {
            if (current.getId().equals(potentialAncestor.getId())) {
                return true;
            }
            
            // Prevent infinite loop if there's somehow a circular reference already
            if (ancestors.contains(current)) {
                log.error("Circular reference detected in feature hierarchy: feature={}, ancestors={}", 
                        feature.getId(), ancestors.stream().map(Feature::getId).collect(Collectors.toList()));
                return true;
            }
            
            ancestors.add(current);
            current = current.getParent();
        }
        
        return false;
    }
}
