package org.hein.api.controller;

import org.hein.api.request.user.UserRequest;
import org.hein.api.response.user.UserResponse;
import org.hein.service.UserService;
import org.hein.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserApi {

    private final UserService userService;

    public UserApi(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Validated @RequestBody UserRequest request, BindingResult result) {
        UserResponse response = userService.create(request);
        return ApiResponse.of(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Validated @RequestBody UserRequest request, BindingResult result) {
        UserResponse response = userService.update(id, request);
        return ApiResponse.of(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ApiResponse.of(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        UserResponse response = userService.getById(id);
        return ApiResponse.of(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        List<UserResponse> users = userService.getAll();
        return ApiResponse.of(users);
    }
}
