package org.hein.api.controller;

import org.hein.api.input.feature.FeatureRequest;
import org.hein.api.output.feature.FeatureResponse;
import org.hein.service.FeatureService;
import org.hein.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/features")
public class FeatureApi {

    private final FeatureService featureService;

    public FeatureApi(FeatureService featureService) {
        this.featureService = featureService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeatureResponse>> create(
            @Validated @RequestBody FeatureRequest request, BindingResult result) {
        FeatureResponse response = featureService.create(request);
        return ApiResponse.of(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureResponse>> update(
            @PathVariable Long id,
            @Validated @RequestBody FeatureRequest request, BindingResult result) {
        FeatureResponse response = featureService.update(id, request);
        return ApiResponse.of(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        featureService.deleteById(id);
        return ApiResponse.of(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureResponse>> getById(@PathVariable Long id) {
        FeatureResponse response = featureService.findById(id);
        return ApiResponse.of(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getAll() {
        List<FeatureResponse> features = featureService.findAll();
        return ApiResponse.of(features);
    }
}
