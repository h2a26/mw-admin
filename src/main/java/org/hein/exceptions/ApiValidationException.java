package org.hein.exceptions;

import lombok.Getter;

import java.io.Serial;
import java.util.List;

@Getter
public class ApiValidationException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final List<String> messages;

	public ApiValidationException(List<String> messages) {
		super();
		this.messages = messages;
	}

}
