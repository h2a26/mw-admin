package org.hein.repository;

import org.hein.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Role entities with support for hierarchical operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find all top-level roles (those without a parent)
     */
    List<Role> findByParentIsNull();

    /**
     * Find by unique code
     */
    Role findByCode(String code);

    /**
     * Find roles that expire within the given time period
     */
    @Query("SELECT r FROM Role r WHERE r.expiryDate IS NOT NULL AND r.expiryDate <= :expiryDate")
    List<Role> findExpiringRoles(LocalDateTime expiryDate);
    
    /**
     * Find all roles that have a specific permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findByPermissionId(Long permissionId);
}