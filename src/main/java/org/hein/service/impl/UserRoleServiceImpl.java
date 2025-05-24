package org.hein.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.api.request.userrole.UserRoleAssignmentRequest;
import org.hein.api.response.userrole.UserRoleResponse;
import org.hein.entity.Role;
import org.hein.entity.User;
import org.hein.entity.UserRole;
import org.hein.entity.UserRoleStatus;
import org.hein.repository.RoleRepository;
import org.hein.repository.UserRepository;
import org.hein.repository.UserRoleRepository;
import org.hein.service.UserRoleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = {"user-roles", "user-permissions"}, allEntries = true)
    public UserRoleResponse assignRole(UserRoleAssignmentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.userId()));
        
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + request.roleId()));
        
        // Check if role assignment already exists
        if (userRoleRepository.existsByUserIdAndRoleId(request.userId(), request.roleId())) {
            throw new IllegalStateException("User already has this role assigned");
        }
        
        // Validate validity period
        if (request.validFrom() != null && request.validTo() != null && 
                request.validFrom().isAfter(request.validTo())) {
            throw new IllegalArgumentException("Valid from date must be before valid to date");
        }
        
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setValidFrom(request.validFrom() != null ? request.validFrom() : LocalDateTime.now());
        userRole.setValidTo(request.validTo());
        // Set the user who assigned this role
        if (request.assignedById() != null) {
            User assignedBy = userRepository.findById(request.assignedById())
                    .orElse(null);
            userRole.setAssignedBy(assignedBy);
        }
        userRole.setAssignmentReason(request.assignmentReason());
        
        // Determine status based on whether the role's permissions require approval
        boolean requiresApproval = role.getPermissions().stream()
                .anyMatch(permission -> permission.isRequiresApproval());
        
        if (requiresApproval) {
            userRole.setStatus(UserRoleStatus.PENDING);
        } else {
            userRole.setStatus(UserRoleStatus.ACTIVE);
        }
        
        UserRole savedUserRole = userRoleRepository.save(userRole);
        return UserRoleResponse.fromEntity(savedUserRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"user-roles", "user-permissions"}, allEntries = true)
    public void removeRole(Long userId, Long roleId) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new EntityNotFoundException("User role assignment not found"));
        
        userRoleRepository.delete(userRole);
    }

    @Override
    @Cacheable(value = "user-roles", key = "'user-' + #userId")
    public List<UserRoleResponse> findByUserId(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .map(UserRoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "user-roles", key = "'role-' + #roleId")
    public List<UserRoleResponse> findByRoleId(Long roleId) {
        List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
        return userRoles.stream()
                .map(UserRoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"user-roles", "user-permissions"}, allEntries = true)
    public UserRoleResponse approveAssignment(Long userRoleId, String approverNotes) {
        UserRole userRole = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new EntityNotFoundException("User role assignment not found: " + userRoleId));
        
        if (userRole.getStatus() != UserRoleStatus.PENDING) {
            throw new IllegalStateException("Cannot approve a role assignment that is not pending");
        }
        
        userRole.setStatus(UserRoleStatus.ACTIVE);
        userRole.setApprovedAt(LocalDateTime.now());
        userRole.setApproverNotes(approverNotes);
        
        UserRole updatedUserRole = userRoleRepository.save(userRole);
        return UserRoleResponse.fromEntity(updatedUserRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"user-roles", "user-permissions"}, allEntries = true)
    public UserRoleResponse rejectAssignment(Long userRoleId, String rejectionReason) {
        UserRole userRole = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new EntityNotFoundException("User role assignment not found: " + userRoleId));
        
        if (userRole.getStatus() != UserRoleStatus.PENDING) {
            throw new IllegalStateException("Cannot reject a role assignment that is not pending");
        }
        
        // Use the reject helper method on UserRole
        userRole.reject(null, rejectionReason);
        
        UserRole updatedUserRole = userRoleRepository.save(userRole);
        return UserRoleResponse.fromEntity(updatedUserRole);
    }

    @Override
    @Cacheable(value = "user-roles", key = "'pending'")
    public List<UserRoleResponse> findPendingApprovals() {
        List<UserRole> pendingRoles = userRoleRepository.findByStatus(UserRoleStatus.PENDING);
        return pendingRoles.stream()
                .map(UserRoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "user-roles", key = "'expiring-' + #days")
    public List<UserRoleResponse> findExpiringAssignments(Integer days) {
        if (days == null || days < 0) {
            days = 30; // Default to 30 days if not specified or invalid
        }
        
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
        List<UserRole> expiringRoles = userRoleRepository.findByValidToBefore(expiryDate);
        
        return expiringRoles.stream()
                .map(UserRoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"user-roles", "user-permissions"}, allEntries = true)
    public UserRoleResponse extendValidity(Long userRoleId, LocalDateTime newExpiryDate) {
        UserRole userRole = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new EntityNotFoundException("User role assignment not found: " + userRoleId));
        
        if (newExpiryDate == null) {
            throw new IllegalArgumentException("New expiry date cannot be null");
        }
        
        if (newExpiryDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New expiry date must be in the future");
        }
        
        // If there's a current expiry date, the new one must be later
        if (userRole.getValidTo() != null && newExpiryDate.isBefore(userRole.getValidTo())) {
            throw new IllegalArgumentException("New expiry date must be later than the current expiry date");
        }
        
        userRole.setValidTo(newExpiryDate);
        UserRole updatedUserRole = userRoleRepository.save(userRole);
        
        return UserRoleResponse.fromEntity(updatedUserRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"user-roles", "user-permissions"}, allEntries = true)
    public void revokeAssignment(Long userRoleId, String revocationReason) {
        UserRole userRole = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new EntityNotFoundException("User role assignment not found: " + userRoleId));
        
        if (userRole.getStatus() == UserRoleStatus.REVOKED) {
            throw new IllegalStateException("Role assignment is already revoked");
        }
        
        // Use the revoke helper method on UserRole
        userRole.revoke(null, revocationReason);
        
        userRoleRepository.save(userRole);
    }
}
