package org.hein.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.api.request.user.UserCreateRequest;
import org.hein.api.request.user.UserRequest;
import org.hein.api.response.user.UserResponse;
import org.hein.entity.Permission;
import org.hein.entity.Role;
import org.hein.entity.User;
import org.hein.entity.UserRole;
// UserRoleStatus is already available through the UserRole import
import org.hein.repository.PermissionRepository;
import org.hein.repository.RoleRepository;
import org.hein.repository.UserRepository;
import org.hein.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse create(UserCreateRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalStateException("Username already exists: " + request.username());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + request.email());
        }
        
        // Create user entity from request
        User user = request.toEntity();
        
        // Encode password
        user.setPassword(passwordEncoder.encode(request.password()));
        
        // Set password expiry if specified
        if (request.passwordExpiryDays() != null && request.passwordExpiryDays() > 0) {
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setPasswordExpiresAt(LocalDateTime.now().plusDays(request.passwordExpiryDays()));
        }
        
        // Save user first to get ID
        User savedUser = userRepository.save(user);
        
        // Assign roles if specified
        if (!request.getRoleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(request.getRoleIds());
            for (Role role : roles) {
                UserRole userRole = new UserRole(savedUser, role);
                savedUser.getUserRoles().add(userRole);
            }
        }
        
        // Assign direct permissions if specified
        if (!request.getDirectPermissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(request.getDirectPermissionIds());
            savedUser.setDirectPermissions(new HashSet<>(permissions));
        }
        
        // Save again with roles and permissions
        User finalUser = userRepository.save(savedUser);
        return UserResponse.fromEntity(finalUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        
        // Update basic fields if provided
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            // Check if new email is already used by another user
            userRepository.findByEmail(request.email())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new IllegalStateException("Email already exists: " + request.email());
                        }
                    });
            user.setEmail(request.email());
        }
        
        if (request.mobilePhone() != null) {
            user.setMobilePhone(request.mobilePhone());
        }
        
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setPasswordChangedAt(LocalDateTime.now());
            
            // Reset password expiry if specified
            if (request.passwordExpiryDays() != null) {
                if (request.passwordExpiryDays() > 0) {
                    user.setPasswordExpiresAt(LocalDateTime.now().plusDays(request.passwordExpiryDays()));
                } else {
                    user.setPasswordExpiresAt(null); // No expiration
                }
            }
        }
        
        if (request.twoFactorEnabled() != null) {
            user.setTwoFactorEnabled(request.twoFactorEnabled());
        }
        
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        
        if (request.locked() != null) {
            user.setLocked(request.locked());
            if (!request.locked()) {
                // Reset failed attempts when unlocking
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
            }
        }
        
        if (request.systemAccount() != null) {
            user.setSystemAccount(request.systemAccount());
        }
        
        // Handle role assignments if specified
        if (request.roleIds() != null) {
            // Get current role IDs
            Set<Long> currentRoleIds = user.getUserRoles().stream()
                    .map(ur -> ur.getRole().getId())
                    .collect(Collectors.toSet());
            
            // Calculate roles to add and remove
            Set<Long> rolesToAdd = request.getRoleIds().stream()
                    .filter(roleId -> !currentRoleIds.contains(roleId))
                    .collect(Collectors.toSet());
            
            Set<Long> rolesToRemove = currentRoleIds.stream()
                    .filter(roleId -> !request.getRoleIds().contains(roleId))
                    .collect(Collectors.toSet());
            
            // Add new roles
            if (!rolesToAdd.isEmpty()) {
                List<Role> rolesToAssign = roleRepository.findAllById(rolesToAdd);
                for (Role role : rolesToAssign) {
                    UserRole userRole = new UserRole(user, role);
                    user.getUserRoles().add(userRole);
                }
            }
            
            // Remove roles
            if (!rolesToRemove.isEmpty()) {
                user.getUserRoles().removeIf(userRole -> 
                        rolesToRemove.contains(userRole.getRole().getId()));
            }
        }
        
        // Handle direct permissions if specified
        if (request.directPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>();
            if (!request.getDirectPermissionIds().isEmpty()) {
                permissions.addAll(permissionRepository.findAllById(request.getDirectPermissionIds()));
            }
            user.setDirectPermissions(permissions);
        }
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Cacheable(value = "users", key = "'all'")
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        
        // Check if this is a system user that shouldn't be deleted
        if (user.isSystemAccount()) {
            throw new IllegalStateException("Cannot delete system account: " + user.getUsername());
        }
        
        userRepository.delete(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse assignRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new EntityNotFoundException("Some roles were not found");
        }
        
        // Get current role IDs to avoid duplicates
        Set<Long> currentRoleIds = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getId())
                .collect(Collectors.toSet());
        
        // Add only new roles
        for (Role role : roles) {
            if (!currentRoleIds.contains(role.getId())) {
                UserRole userRole = new UserRole(user, role);
                user.getUserRoles().add(userRole);
            }
        }
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse removeRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        // Remove the specified roles
        user.getUserRoles().removeIf(userRole -> 
                roleIds.contains(userRole.getRole().getId()));
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse assignDirectPermissions(Long userId, Set<Long> permissionIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new EntityNotFoundException("Some permissions were not found");
        }
        
        // Add to existing permissions
        Set<Permission> currentPermissions = user.getDirectPermissions();
        currentPermissions.addAll(permissions);
        user.setDirectPermissions(currentPermissions);
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse removeDirectPermissions(Long userId, Set<Long> permissionIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        // Remove the specified permissions
        user.getDirectPermissions().removeIf(permission -> 
                permissionIds.contains(permission.getId()));
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        
        // Reset password expiry based on default policy (30 days)
        // This could be configured from application properties
        int expiryDays = 30;
        if (expiryDays > 0) {
            user.setPasswordExpiresAt(LocalDateTime.now().plusDays(expiryDays));
        }
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        
        // Typically when admin resets a password, user should change it on next login
        user.setPasswordExpiresAt(LocalDateTime.now()); // Expired immediately
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "user-roles"}, allEntries = true)
    public UserResponse setLocked(Long userId, boolean locked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        user.setLocked(locked);
        if (!locked) {
            // Reset failed attempts when unlocking
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
        } else {
            // When manually locking, set lock for 24 hours by default
            user.setLockedUntil(LocalDateTime.now().plusHours(24));
        }
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    public List<UserResponse> findByRoleId(Long roleId) {
        // Validate that role exists
        roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));
        
        List<User> users = userRepository.findByRoleId(roleId);
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> findByRoleExpiringInDays(int days) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
        List<User> users = userRepository.findByRoleExpiringBefore(expiryDate);
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
