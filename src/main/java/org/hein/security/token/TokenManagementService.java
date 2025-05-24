package org.hein.security.token;

import lombok.RequiredArgsConstructor;
import org.hein.api.request.auth.TokenRefreshForm;
import org.hein.api.request.auth.TokenRequestForm;
import org.hein.api.request.auth.TokenRevokeForm;
import org.hein.api.response.auth.TokenResponse;
import org.hein.commons.enum_.TokenType;
import org.hein.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenManagementService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenParser jwtTokenParser;
	private final JwtTokenGenerator jwtTokenGenerator;
	private final JtiTokenStore jtiTokenStore;
	private final UserService userService;

	@Transactional(readOnly = true)
	public TokenResponse generate(TokenRequestForm form) {
		var usernamePasswordToken = UsernamePasswordAuthenticationToken.unauthenticated(form.username(), form.password());
		var authentication = authenticationManager.authenticate(usernamePasswordToken);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		return generateTokens(authentication);
	}

	@Transactional(readOnly = true)
	public TokenResponse refresh(TokenRefreshForm form) {
		var authentication = jwtTokenParser.parse(TokenType.Refresh, form.refreshToken());
		var username = authentication.getName();

		jtiTokenStore.revokeTokens(username);

		return generateTokens(authentication);
	}

	private TokenResponse generateTokens(Authentication authentication) {
		var username = authentication.getName();
		var user = userService.findByUsername(username);

		// Generate a new jti for refresh token
		var accessJti = UUID.randomUUID().toString();
		var refreshJti = UUID.randomUUID().toString();

		var accessToken = jwtTokenGenerator.generateAccessToken(authentication, accessJti);
		var refreshToken = jwtTokenGenerator.generateRefreshToken(authentication, refreshJti);

		jtiTokenStore.storeAccessJti(accessJti, username);
		jtiTokenStore.storeRefreshJti(refreshJti, username);

		return TokenResponse.from(user, accessToken, refreshToken);
	}

	public void revoke(TokenRevokeForm form) {
		var authentication = jwtTokenParser.parse(TokenType.Refresh, form.refreshToken());
		var username = authentication.getName();
		jtiTokenStore.revokeTokens(username);
	}
}
