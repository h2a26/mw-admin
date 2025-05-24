package org.hein.api.controller;

import org.hein.api.request.permission.PermissionRequest;
import org.hein.api.response.permission.PermissionResponse;
import org.hein.service.PermissionService;
import org.hein.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionApi {

    private final PermissionService permissionService;

    public PermissionApi(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> create(
            @Validated @RequestBody PermissionRequest request, BindingResult result) {
        PermissionResponse response = permissionService.create(request);
        return ApiResponse.of(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @PathVariable Long id,
            @Validated @RequestBody PermissionRequest request, BindingResult result) {
        PermissionResponse response = permissionService.update(id, request);
        return ApiResponse.of(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissionService.deleteById(id);
        return ApiResponse.of(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(@PathVariable Long id) {
        PermissionResponse response = permissionService.findById(id);
        return ApiResponse.of(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAll() {
        List<PermissionResponse> permissions = permissionService.findAll();
        return ApiResponse.of(permissions);
    }
}
