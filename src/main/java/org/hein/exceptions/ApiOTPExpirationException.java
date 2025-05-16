package org.hein.exceptions;

public class ApiOTPExpirationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ApiOTPExpirationException(String message) {
		super(message);
	}
}
