package org.hein.service;

import org.hein.api.input.feature.FeatureRequest;
import org.hein.api.output.feature.FeatureResponse;

import java.util.List;

public interface FeatureService {
    FeatureResponse create(FeatureRequest request);
    FeatureResponse update(Long id, FeatureRequest request);
    void delete(Long id);
    FeatureResponse getById(Long id);
    List<FeatureResponse> getAll();
}

