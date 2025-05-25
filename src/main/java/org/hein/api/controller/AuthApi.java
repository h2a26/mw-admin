package org.hein.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hein.api.request.auth.TokenRefreshForm;
import org.hein.api.request.auth.TokenRequestForm;
import org.hein.api.request.auth.TokenRevokeForm;
import org.hein.api.response.auth.TokenResponse;
import org.hein.security.token.TokenManagementService;
import org.hein.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and token management")
public class AuthApi {

    private final TokenManagementService tokenService;

    public AuthApi(TokenManagementService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and generate access tokens")
    public ResponseEntity<ApiResponse<TokenResponse>> generate(@Valid @RequestBody TokenRequestForm form) {
        TokenResponse tokenResponse = tokenService.generate(form);
        return ApiResponse.of(tokenResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a valid refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody TokenRefreshForm form) {
        TokenResponse tokenResponse = tokenService.refresh(form);
        return ApiResponse.of(tokenResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and revoke tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRevokeForm form) {
        tokenService.revoke(form);
        return ApiResponse.of();
    }
}
