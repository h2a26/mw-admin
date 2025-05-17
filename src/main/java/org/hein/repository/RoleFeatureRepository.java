package org.hein.repository;

import org.hein.entity.RoleFeature;
import org.hein.entity.Role;
import org.hein.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleFeatureRepository extends JpaRepository<RoleFeature, Long> {
    List<RoleFeature> findByRole(Role role);
    List<RoleFeature> findByFeature(Feature feature);
}
