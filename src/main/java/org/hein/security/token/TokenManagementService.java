package org.hein.security.token;

import lombok.RequiredArgsConstructor;
import org.hein.api.input.auth.TokenRefreshForm;
import org.hein.api.input.auth.TokenRequestForm;
import org.hein.api.output.auth.TokenResponse;
import org.hein.commons.enum_.TokenType;
import org.hein.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenManagementService {

	private final AuthenticationManager authenticationManager;

	private final JwtTokenParser jwtTokenParser;

	private final JwtTokenGenerator jwtTokenGenerator;

    private final UserService userService;

    @Transactional(readOnly = true)
	public TokenResponse generate(TokenRequestForm form) {

		var usernamePasswordToken = UsernamePasswordAuthenticationToken.unauthenticated(form.username(), form.password());
		var authentication = authenticationManager.authenticate(usernamePasswordToken);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		return getResponse(authentication);
	}

	@Transactional(readOnly = true)
	public TokenResponse refresh(TokenRefreshForm form) {
		var authentication = jwtTokenParser.parse(TokenType.Refresh, form.refreshToken());
		return getResponse(authentication);
	}


	private TokenResponse getResponse(Authentication authentication) {

		var user = userService.findByUsername(authentication.getName());

		var accessToken = jwtTokenGenerator.generate(TokenType.Access, authentication);
		var refreshToken = jwtTokenGenerator.generate(TokenType.Refresh, authentication);

		return TokenResponse.from(user, accessToken, refreshToken);
	}
}
