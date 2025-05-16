package org.hein.api.output.auth;

import org.hein.entity.User;

public record TokenResponse (
		String email,
		String accessToken,
		String refreshToken ) {

	public static TokenResponse from(User user, String accessToken, String refreshToken) {
		return new TokenResponse(user.getEmail(),
				accessToken,
				refreshToken);
	}
}
