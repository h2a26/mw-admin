package org.hein.service.impl;

import jakarta.transaction.Transactional;
import org.hein.api.request.role.RoleRequest;
import org.hein.api.response.role.RoleResponse;
import org.hein.entity.Permission;
import org.hein.entity.Role;
import org.hein.repository.PermissionRepository;
import org.hein.repository.RoleRepository;
import org.hein.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public RoleResponse create(RoleRequest request) {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));

        if (permissions.size() != request.permissionIds().size()) {
            throw new IllegalArgumentException("One or more permission IDs are invalid.");
        }

        Role role = Role.builder()
                .name(request.name())
                .description(request.description())
                .permissions(permissions)
                .build();

        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));
        if (permissions.size() != request.permissionIds().size()) {
            throw new IllegalArgumentException("One or more permission IDs are invalid.");
        }

        role.setName(request.name());
        role.setDescription(request.description());

        // Clear existing permissions and add new ones
        role.getRolePermissions().clear();
        permissions.forEach(role::addPermission);

        return RoleResponse.from(roleRepository.save(role));
    }


    @Override
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse findById(Long id) {
        return roleRepository.findById(id)
                .map(RoleResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }
}
