package org.hein.repository;

import org.hein.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {
    
    /**
     * Find all top-level features (those without a parent)
     */
    List<Feature> findByParentIsNull();
}