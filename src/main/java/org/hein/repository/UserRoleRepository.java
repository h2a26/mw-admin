package org.hein.repository;

import org.hein.entity.UserRole;
import org.hein.entity.UserRoleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing UserRole entities with advanced RBAC capabilities
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Find all role assignments for a specific user
     */
    List<UserRole> findByUserId(Long userId);
    
    /**
     * Find all users assigned to a specific role
     */
    List<UserRole> findByRoleId(Long roleId);
    
    /**
     * Find a specific role assignment for a user and role combination
     */
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
    
    /**
     * Check if a user has a specific role assigned
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    
    /**
     * Find all role assignments with a specific status (e.g., PENDING for approval workflow)
     */
    List<UserRole> findByStatus(UserRoleStatus status);
    
    /**
     * Find all active role assignments for a user
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.status = org.hein.entity.UserRoleStatus.ACTIVE "+
           "AND (ur.validFrom IS NULL OR ur.validFrom <= CURRENT_TIMESTAMP) "+
           "AND (ur.validTo IS NULL OR ur.validTo > CURRENT_TIMESTAMP)")
    List<UserRole> findActiveRolesByUserId(@Param("userId") Long userId);
    
    /**
     * Find role assignments that will expire before a specified date
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.status = org.hein.entity.UserRoleStatus.ACTIVE "+
           "AND ur.validTo IS NOT NULL AND ur.validTo <= :expiryDate")
    List<UserRole> findByValidToBefore(@Param("expiryDate") LocalDateTime expiryDate);
}