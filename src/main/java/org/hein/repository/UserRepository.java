package org.hein.repository;

import org.hein.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find users who have a specific role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur WHERE ur.role.id = :roleId AND ur.status = 'ACTIVE' " +
           "AND (ur.validFrom IS NULL OR ur.validFrom <= CURRENT_TIMESTAMP) " +
           "AND (ur.validTo IS NULL OR ur.validTo > CURRENT_TIMESTAMP)")
    List<User> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Find users with roles that will expire within the specified number of days
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur WHERE ur.status = 'ACTIVE' " +
           "AND ur.validTo IS NOT NULL AND ur.validTo <= :expiryDate")
    List<User> findByRoleExpiringBefore(@Param("expiryDate") LocalDateTime expiryDate);
}