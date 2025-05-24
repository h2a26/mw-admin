package org.hein.api.response.auth;

import org.hein.entity.User;

public record TokenResponse (
		String username,
		String accessToken,
		String refreshToken ) {

	public static TokenResponse from(User user, String accessToken, String refreshToken) {
		return new TokenResponse(user.getUsername(), accessToken, refreshToken);
	}
}
