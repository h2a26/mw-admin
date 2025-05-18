package org.hein.exceptions;

import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

public class ApiJwtTokenInvalidationException extends AuthenticationException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ApiJwtTokenInvalidationException(String msg) {
		super(msg);
	}

	public ApiJwtTokenInvalidationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
