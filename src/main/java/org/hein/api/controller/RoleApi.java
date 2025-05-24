package org.hein.api.controller;

import org.hein.api.request.role.RoleRequest;
import org.hein.api.response.role.RoleResponse;
import org.hein.service.RoleService;
import org.hein.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleApi {

    private final RoleService roleService;

    public RoleApi(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Validated @RequestBody RoleRequest request, BindingResult result) {
        RoleResponse response = roleService.create(request);
        return ApiResponse.of(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable Long id,
            @Validated @RequestBody RoleRequest request, BindingResult result) {
        RoleResponse response = roleService.update(id, request);
        return ApiResponse.of(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.deleteById(id);
        return ApiResponse.of(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getById(@PathVariable Long id) {
        RoleResponse response = roleService.findById(id);
        return ApiResponse.of(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAll() {
        List<RoleResponse> roles = roleService.findAll();
        return ApiResponse.of(roles);
    }
}
