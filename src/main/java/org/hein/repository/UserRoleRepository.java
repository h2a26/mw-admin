package org.hein.repository;

import org.hein.entity.UserRole;
import org.hein.entity.User;
import org.hein.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByRole(Role role);
}
