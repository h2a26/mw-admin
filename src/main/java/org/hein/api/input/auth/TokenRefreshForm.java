package org.hein.api.input.auth;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshForm(
		@NotBlank(message = "Please enter login id.")
		String username,
		@NotBlank(message = "Please enter refresh token.")
		String refreshToken) {

}
