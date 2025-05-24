package org.hein.api.request.auth;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshForm(
		@NotBlank(message = "Please enter refresh token.")
		String refreshToken) {

}
