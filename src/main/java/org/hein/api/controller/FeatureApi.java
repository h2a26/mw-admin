package org.hein.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hein.api.request.feature.FeatureCreateRequest;
import org.hein.api.response.feature.FeatureResponse;
import org.hein.service.FeatureService;
import org.hein.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Feature resources
 */
@RestController
@RequestMapping("/api/v1/features")
@Tag(name = "Feature Management", description = "APIs for managing features")
public class FeatureApi {

    private final FeatureService featureService;

    public FeatureApi(FeatureService featureService) {
        this.featureService = featureService;
    }

    /**
     * Create a new feature
     */
    @PostMapping
    @Operation(summary = "Create a new feature")
    @PreAuthorize("hasAuthority('features:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<FeatureResponse>> create(
            @Valid @RequestBody FeatureCreateRequest request) {
        FeatureResponse response = featureService.create(request);
        return ApiResponse.of(response, HttpStatus.CREATED, null);
    }

    /**
     * Update an existing feature
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing feature")
    @PreAuthorize("hasAuthority('features:UPDATE')")
    public ResponseEntity<ApiResponse<FeatureResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FeatureCreateRequest request) {
        FeatureResponse response = featureService.update(id, request);
        return ApiResponse.of(response);
    }

    /**
     * Delete a feature by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a feature")
    @PreAuthorize("hasAuthority('features:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        featureService.deleteById(id);
        return ApiResponse.of(null, HttpStatus.NO_CONTENT, null);
    }

    /**
     * Toggle a feature's enabled status
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle a feature's enabled status")
    @PreAuthorize("hasAuthority('features:UPDATE')")
    public ResponseEntity<ApiResponse<FeatureResponse>> toggleStatus(@PathVariable Long id) {
        FeatureResponse response = featureService.toggleStatus(id);
        return ApiResponse.of(response);
    }

    /**
     * Get a feature by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a feature by ID")
    @PreAuthorize("hasAuthority('features:VIEW')")
    public ResponseEntity<ApiResponse<FeatureResponse>> getById(
            @PathVariable Long id,
            @Parameter(description = "Whether to include child features")
            @RequestParam(required = false, defaultValue = "false") boolean includeChildren) {
        FeatureResponse response = featureService.findById(id, includeChildren);
        return ApiResponse.of(response);
    }

    /**
     * Get all features
     */
    @GetMapping
    @Operation(summary = "Get all features")
    @PreAuthorize("hasAuthority('features:VIEW')")
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getAll(
            @Parameter(description = "Return only top-level features")
            @RequestParam(required = false, defaultValue = "false") boolean topLevelOnly,
            @Parameter(description = "Whether to include child features")
            @RequestParam(required = false, defaultValue = "false") boolean includeChildren) {
        List<FeatureResponse> features = featureService.findAll(topLevelOnly, includeChildren);
        return ApiResponse.of(features);
    }
    
    /**
     * Get child features of a parent feature
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "Get child features of a parent feature")
    @PreAuthorize("hasAuthority('features:VIEW')")
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getChildren(@PathVariable Long id) {
        List<FeatureResponse> children = featureService.findChildren(id);
        return ApiResponse.of(children);
    }
    
    /**
     * Move a feature to a new parent
     */
    @PutMapping("/{id}/parent/{parentId}")
    @Operation(summary = "Move a feature to a new parent")
    @PreAuthorize("hasAuthority('features:UPDATE')")
    public ResponseEntity<ApiResponse<FeatureResponse>> moveToParent(
            @PathVariable Long id,
            @PathVariable Long parentId) {
        FeatureResponse response = featureService.moveToParent(id, parentId);
        return ApiResponse.of(response);
    }
    
    /**
     * Make a feature a top-level feature (remove parent)
     */
    @DeleteMapping("/{id}/parent")
    @Operation(summary = "Make a feature a top-level feature (remove parent)")
    @PreAuthorize("hasAuthority('features:UPDATE')")
    public ResponseEntity<ApiResponse<FeatureResponse>> removeParent(@PathVariable Long id) {
        FeatureResponse response = featureService.removeParent(id);
        return ApiResponse.of(response);
    }
}
