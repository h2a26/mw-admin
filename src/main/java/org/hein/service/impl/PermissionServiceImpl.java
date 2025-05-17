package org.hein.service.impl;

import jakarta.transaction.Transactional;
import org.hein.api.input.permission.PermissionRequest;
import org.hein.api.output.permission.PermissionResponse;
import org.hein.entity.Action;
import org.hein.entity.Feature;
import org.hein.entity.Permission;
import org.hein.repository.FeatureRepository;
import org.hein.repository.PermissionRepository;
import org.hein.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final FeatureRepository featureRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository, FeatureRepository featureRepository) {
        this.permissionRepository = permissionRepository;
        this.featureRepository = featureRepository;
    }

    @Override
    public PermissionResponse create(PermissionRequest request) {
        Feature feature = featureRepository.findById(request.featureId())
                .orElseThrow(() -> new IllegalArgumentException("Feature not found with id: " + request.featureId()));

        Permission permission = Permission.builder()
                .feature(feature)
                .action(Action.valueOf(request.action()))
                .build();

        return PermissionResponse.from(permissionRepository.save(permission));
    }

    @Override
    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + id));

        Feature feature = featureRepository.findById(request.featureId())
                .orElseThrow(() -> new IllegalArgumentException("Feature not found with id: " + request.featureId()));

        existing.setFeature(feature);
        existing.setAction(Action.valueOf(request.action()));

        return PermissionResponse.from(permissionRepository.save(existing));
    }


    @Override
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream()
                .map(PermissionResponse::from)
                .toList();
    }

    @Override
    public PermissionResponse findById(Long id) {
        return permissionRepository.findById(id)
                .map(PermissionResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new IllegalArgumentException("Permission not found with id: " + id);
        }
        permissionRepository.deleteById(id);
    }
}
