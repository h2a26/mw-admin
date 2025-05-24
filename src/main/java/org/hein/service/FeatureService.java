package org.hein.service;

import org.hein.api.request.feature.FeatureRequest;
import org.hein.api.response.feature.FeatureResponse;

import java.util.List;

public interface FeatureService {
    FeatureResponse create(FeatureRequest request);
    FeatureResponse update(Long id, FeatureRequest request);
    void deleteById(Long id);
    FeatureResponse findById(Long id);
    List<FeatureResponse> findAll();
}

