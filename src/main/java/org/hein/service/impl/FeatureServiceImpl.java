package org.hein.service.impl;

import org.hein.api.input.feature.FeatureRequest;
import org.hein.api.output.feature.FeatureResponse;
import org.hein.entity.Feature;
import org.hein.repository.FeatureRepository;
import org.hein.service.FeatureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;

    public FeatureServiceImpl(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    @Override
    public FeatureResponse create(FeatureRequest request) {
        Feature feature = new Feature();
        feature.setName(request.name());
        feature.setDescription(request.description());
        return FeatureResponse.from(featureRepository.save(feature));
    }

    @Override
    public FeatureResponse update(Long id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found."));
        feature.setName(request.name());
        feature.setDescription(request.description());
        return FeatureResponse.from(featureRepository.save(feature));
    }

    @Override
    public void delete(Long id) {
        featureRepository.deleteById(id);
    }

    @Override
    public FeatureResponse getById(Long id) {
        return featureRepository.findById(id)
                .map(FeatureResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found."));
    }

    @Override
    public List<FeatureResponse> getAll() {
        return featureRepository.findAll().stream()
                .map(FeatureResponse::from)
                .toList();
    }
}
