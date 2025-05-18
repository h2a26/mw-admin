package org.hein.exceptions;

import java.io.Serial;

public class ApiBusinessException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ApiBusinessException(String message) {
		super(message);
	}
}
