package org.hein.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.api.request.role.RoleCreateRequest;
import org.hein.api.response.role.RoleResponse;
import org.hein.entity.Permission;
import org.hein.entity.Role;
import org.hein.repository.PermissionRepository;
import org.hein.repository.RoleRepository;
import org.hein.service.RoleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse create(RoleCreateRequest request) {
        // Check if code is already in use
        if (roleRepository.findByCode(request.code()) != null) {
            throw new IllegalStateException("Role code already in use: " + request.code());
        }
        
        Role role = new Role();
        role.setName(request.name());
        role.setCode(request.code());
        role.setDescription(request.description());
        role.setPriority(request.priority() != null ? request.priority() : 0);
        role.setSystemRole(request.systemRole() != null ? request.systemRole() : false);
        role.setDefaultRole(request.defaultRole() != null ? request.defaultRole() : false);
        // All roles are active by default
        
        if (request.expiryDate() != null) {
            role.setExpiryDate(request.expiryDate());
        }
        
        // Set parent role if provided
        if (request.parentId() != null) {
            Role parent = roleRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent role not found: " + request.parentId()));
            role.setParent(parent);
        }
        
        // Add permissions if provided
        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            Set<Permission> permissions = request.permissionIds().stream()
                    .map(id -> permissionRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + id)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        Role savedRole = roleRepository.save(role);
        return RoleResponse.fromEntity(savedRole, true, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse update(Long id, RoleCreateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        // If code is changing, check if it's already in use
        if (!role.getCode().equals(request.code()) && roleRepository.findByCode(request.code()) != null) {
            throw new IllegalStateException("Role code already in use: " + request.code());
        }
        
        role.setName(request.name());
        role.setCode(request.code());
        role.setDescription(request.description());
        role.setPriority(request.priority() != null ? request.priority() : 0);
        role.setSystemRole(request.systemRole() != null ? request.systemRole() : false);
        role.setDefaultRole(request.defaultRole() != null ? request.defaultRole() : false);
        // Update expiry date
        role.setExpiryDate(request.expiryDate()); // Can be null
        
        // Parent can't be the role itself
        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new IllegalArgumentException("Role cannot be its own parent");
        }
        
        // Update parent if provided and different from current
        if (request.parentId() != null) {
            Role parent = roleRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent role not found: " + request.parentId()));
            
            // Check for circular references
            if (isAncestor(role, parent)) {
                throw new IllegalArgumentException("Circular reference detected in role hierarchy");
            }
            
            role.setParent(parent);
        } else if (request.parentId() == null && role.getParent() != null) {
            // Remove parent if null was provided
            role.setParent(null);
        }
        
        // Update permissions if provided
        if (request.permissionIds() != null) {
            Set<Permission> permissions = request.permissionIds().stream()
                    .map(permId -> permissionRepository.findById(permId)
                            .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        Role updatedRole = roleRepository.save(role);
        return RoleResponse.fromEntity(updatedRole, true, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public void deleteById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        // Check if role has children
        if (!role.getChildRoles().isEmpty()) {
            throw new IllegalStateException("Cannot delete role with children. Remove children first.");
        }
        
        // Check if role is assigned to any users
        if (!role.getUserRoles().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that is assigned to users. Remove from users first.");
        }
        
        roleRepository.delete(role);
    }

    @Override
    @Cacheable(value = "roles", key = "#id + '-' + #includePermissions + '-' + #includeChildRoles")
    public RoleResponse findById(Long id, boolean includePermissions, boolean includeChildRoles) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        return RoleResponse.fromEntity(role, includePermissions, includeChildRoles);
    }

    @Override
    @Cacheable(value = "roles", key = "'all-' + #topLevelOnly + '-' + #includePermissions + '-' + #includeChildRoles")
    public List<RoleResponse> findAll(boolean topLevelOnly, boolean includePermissions, boolean includeChildRoles) {
        List<Role> roles;
        
        if (topLevelOnly) {
            roles = roleRepository.findByParentIsNull();
        } else {
            roles = roleRepository.findAll();
        }
        
        return roles.stream()
                .map(role -> RoleResponse.fromEntity(role, includePermissions, includeChildRoles))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "roles", key = "'children-' + #id")
    public List<RoleResponse> findChildRoles(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        return role.getChildRoles().stream()
                .map(child -> RoleResponse.fromEntity(child, false, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse moveToParent(Long id, Long parentId) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        if (id.equals(parentId)) {
            throw new IllegalArgumentException("Role cannot be its own parent");
        }
        
        Role parent = roleRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent role not found: " + parentId));
        
        // Check for circular references
        if (isAncestor(role, parent)) {
            throw new IllegalArgumentException("Circular reference detected in role hierarchy");
        }
        
        role.setParent(parent);
        Role updatedRole = roleRepository.save(role);
        
        return RoleResponse.fromEntity(updatedRole, false, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse removeParent(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        if (role.getParent() == null) {
            return RoleResponse.fromEntity(role, false, false);
        }
        
        role.setParent(null);
        Role updatedRole = roleRepository.save(role);
        
        return RoleResponse.fromEntity(updatedRole, false, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse addPermissions(Long id, Set<Long> permissionIds) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        Set<Permission> currentPermissions = role.getPermissions();
        if (currentPermissions == null) {
            currentPermissions = new HashSet<>();
        }
        
        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permissionId));
            currentPermissions.add(permission);
        }
        
        role.setPermissions(currentPermissions);
        Role updatedRole = roleRepository.save(role);
        
        return RoleResponse.fromEntity(updatedRole, true, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse removePermissions(Long id, Set<Long> permissionIds) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        Set<Permission> currentPermissions = role.getPermissions();
        if (currentPermissions == null || currentPermissions.isEmpty()) {
            return RoleResponse.fromEntity(role, true, false);
        }
        
        currentPermissions.removeIf(permission -> permissionIds.contains(permission.getId()));
        role.setPermissions(currentPermissions);
        
        Role updatedRole = roleRepository.save(role);
        return RoleResponse.fromEntity(updatedRole, true, false);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse toggleStatus(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        
        // Toggle the default role status as a proxy for enabled/disabled
        // since Role doesn't have an explicit enabled property
        role.setDefaultRole(!role.isDefaultRole());
        Role updatedRole = roleRepository.save(role);
        
        return RoleResponse.fromEntity(updatedRole, false, false);
    }

    @Override
    @Cacheable(value = "roles", key = "'expiring-' + #days")
    public List<RoleResponse> findExpiringRoles(Integer days) {
        if (days == null || days < 0) {
            days = 30; // Default to 30 days if not specified or invalid
        }
        
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
        List<Role> expiringRoles = roleRepository.findExpiringRoles(expiryDate);
        
        return expiringRoles.stream()
                .map(role -> RoleResponse.fromEntity(role, false, false))
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to check if a potential parent is actually an ancestor of the role
     * This prevents circular references in the hierarchy
     */
    private boolean isAncestor(Role role, Role potentialAncestor) {
        if (role == null || potentialAncestor == null) {
            return false;
        }
        
        List<Role> ancestors = new ArrayList<>();
        Role current = role.getParent();
        
        while (current != null) {
            if (current.getId().equals(potentialAncestor.getId())) {
                return true;
            }
            
            // Prevent infinite loop if there's somehow a circular reference already
            if (ancestors.contains(current)) {
                log.error("Circular reference detected in role hierarchy: role={}, ancestors={}", 
                        role.getId(), ancestors.stream().map(Role::getId).collect(Collectors.toList()));
                return true;
            }
            
            ancestors.add(current);
            current = current.getParent();
        }
        
        return false;
    }
}
