package org.hein.exceptions;

import java.io.Serial;

public class ApiRateLimitedException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ApiRateLimitedException(String message) {
		super(message);
	}
}
