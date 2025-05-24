package org.hein.repository;

import org.hein.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Permission entities with enhanced querying capabilities
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Check if a permission exists with the given feature ID and action
     */
    boolean existsByFeatureIdAndAction(Long featureId, String action);
    
    /**
     * Find permissions by feature ID
     */
    List<Permission> findByFeatureId(Long featureId);
    
    /**
     * Find permissions that require approval
     */
    List<Permission> findByRequiresApprovalTrue();
    
    /**
     * Find permissions associated with a specific role
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(Long roleId);
    
    /**
     * Find permissions by constraint policy
     */
    List<Permission> findByConstraintPolicy(String constraintPolicy);
}