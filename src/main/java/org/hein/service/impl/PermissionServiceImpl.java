package org.hein.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.api.request.permission.PermissionCreateRequest;
import org.hein.api.response.permission.PermissionResponse;
import org.hein.entity.Action;
import org.hein.entity.Feature;
import org.hein.entity.Permission;
import org.hein.repository.FeatureRepository;
import org.hein.repository.PermissionRepository;
import org.hein.service.PermissionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final FeatureRepository featureRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionResponse create(PermissionCreateRequest request) {
        // Validate feature exists
        Feature feature = featureRepository.findById(request.featureId())
                .orElseThrow(() -> new EntityNotFoundException("Feature not found: " + request.featureId()));
        
        // Check if permission with same feature and action already exists
        Action actionEnum = Action.valueOf(request.action().toUpperCase());
        if (permissionRepository.existsByFeatureIdAndAction(request.featureId(), actionEnum)) {
            throw new IllegalStateException("Permission already exists for feature " + feature.getName() + " and action " + request.action());
        }
        
        Permission permission = new Permission();
        permission.setFeature(feature);
        permission.setAction(Action.valueOf(request.action().toUpperCase()));
        permission.setDescription(request.description());
        permission.setRequiresApproval(request.requiresApproval());
        permission.setConstraintPolicy(request.constraintPolicy());
        
        Permission savedPermission = permissionRepository.save(permission);
        return PermissionResponse.fromEntity(savedPermission);
    }

    @Override
    @Transactional
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionResponse update(Long id, PermissionCreateRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + id));
        
        // Feature and action are immutable, only update description and approval settings
        permission.setDescription(request.description());
        permission.setRequiresApproval(request.requiresApproval());
        permission.setConstraintPolicy(request.constraintPolicy());
        
        Permission updatedPermission = permissionRepository.save(permission);
        return PermissionResponse.fromEntity(updatedPermission);
    }

    @Override
    @Transactional
    @CacheEvict(value = "permissions", allEntries = true)
    public void deleteById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + id));
        
        // Check if permission is used by any roles before deleting
        if (!permission.getRoles().isEmpty()) {
            throw new IllegalStateException("Cannot delete permission that is assigned to roles. Remove from roles first.");
        }
        
        permissionRepository.delete(permission);
    }

    @Override
    @Cacheable(value = "permissions", key = "#id")
    public PermissionResponse findById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + id));
        
        return PermissionResponse.fromEntity(permission);
    }

    @Override
    @Cacheable(value = "permissions", key = "'all'")
    public List<PermissionResponse> findAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "permissions", key = "'feature-' + #featureId")
    public List<PermissionResponse> findByFeatureId(Long featureId) {
        // Verify feature exists
        if (!featureRepository.existsById(featureId)) {
            throw new EntityNotFoundException("Feature not found: " + featureId);
        }
        
        List<Permission> permissions = permissionRepository.findByFeatureId(featureId);
        return permissions.stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "permissions", key = "'requires-approval'")
    public List<PermissionResponse> findRequiresApproval() {
        List<Permission> permissions = permissionRepository.findByRequiresApprovalTrue();
        return permissions.stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
