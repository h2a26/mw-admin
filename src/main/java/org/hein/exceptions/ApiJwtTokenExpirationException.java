package org.hein.exceptions;

import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

public class ApiJwtTokenExpirationException extends AuthenticationException {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public ApiJwtTokenExpirationException(String msg) {
		super(msg);
	}

}
