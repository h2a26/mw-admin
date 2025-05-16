package org.hein.api.controller;

import org.hein.api.input.auth.TokenRefreshForm;
import org.hein.api.input.auth.TokenRequestForm;
import org.hein.api.input.auth.UserRegistrationRequest;
import org.hein.api.output.auth.TokenResponse;
import org.hein.api.output.auth.UserRegistrationResponse;
import org.hein.security.token.TokenManagementService;
import org.hein.service.AuthService;
import org.hein.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthApi {

    private final AuthService authService;
    private final TokenManagementService tokenService;

    public AuthApi(AuthService authService, TokenManagementService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> register(@Validated @RequestBody UserRegistrationRequest userRegistrationRequest, BindingResult result) {
        UserRegistrationResponse userRegistrationResponse = authService.registerUser(userRegistrationRequest);
        return ApiResponse.of(userRegistrationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> generate(@Validated @RequestBody TokenRequestForm form, BindingResult result) {
        TokenResponse tokenResponse = tokenService.generate(form);
        return ApiResponse.of(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Validated @RequestBody TokenRefreshForm form, BindingResult result) {
        TokenResponse tokenResponse = tokenService.refresh(form);
        return ApiResponse.of(tokenResponse);
    }
}
