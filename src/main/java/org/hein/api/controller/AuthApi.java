package org.hein.api.controller;

import org.hein.api.input.auth.TokenRefreshForm;
import org.hein.api.input.auth.TokenRequestForm;
import org.hein.api.input.auth.TokenRevokeForm;
import org.hein.api.output.auth.TokenResponse;
import org.hein.security.token.TokenManagementService;
import org.hein.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApi {

    private final TokenManagementService tokenService;

    public AuthApi(TokenManagementService tokenService) {
        this.tokenService = tokenService;
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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Validated @RequestBody TokenRevokeForm form, BindingResult result) {
        tokenService.revoke(form);
        return ApiResponse.of(null);
    }
}
