package org.hein.exceptions.handler;

import org.hein.exceptions.*;
import org.hein.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

	@ExceptionHandler(ApiValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ApiResponse<List<String>>> handle(ApiValidationException e) {
		return ApiResponse.of(e.getMessages(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ApiRateLimitedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ApiResponse<String>> handle(ApiRateLimitedException e) {
		return ApiResponse.of("You are being rate limited. Please try again later.", HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(ApiBusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ApiResponse<List<String>>> handle(ApiBusinessException e) {
		return ApiResponse.of(List.of("A business rule was violated. Please review your request."), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ApiJwtTokenExpirationException.class)
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	public ResponseEntity<ApiResponse<String>> handle(ApiJwtTokenExpirationException e) {
		return ApiResponse.of("Access token has expired. Please refresh your token.", HttpStatus.REQUEST_TIMEOUT);
	}

	@ExceptionHandler(ApiJwtTokenInvalidationException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ResponseEntity<ApiResponse<List<String>>> handle(ApiJwtTokenInvalidationException e) {
		log.warn("Invalid token usage: {}", e.getMessage());
		return ApiResponse.of(List.of("Token is invalid or has been tampered."), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ApiResponse<List<String>>> handle(AuthenticationException e) {
		log.warn("Authentication failed: {}", e.getClass().getSimpleName());

		List<String> messages = switch (e) {
			case BadCredentialsException ex -> List.of("Incorrect password. Please try again.");
			case UsernameNotFoundException ex -> List.of("Login ID not found.");
			case DisabledException ex -> List.of("Your account is currently disabled.");
			case AccountExpiredException ex -> List.of("Your account has expired.");
			default -> List.of("Authentication is required for this action.");
		};

		return ApiResponse.of(messages, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ApiResponse<String>> handle(AccessDeniedException e) {
		log.warn("Access denied: {}", e.getMessage());
		return ApiResponse.of("You do not have permission to perform this action.", HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ApiResponse<List<String>>> handle(InvalidDataAccessApiUsageException e) {
		log.error("Invalid data access usage", e);
		return ApiResponse.of(List.of("The requested resource could not be found or accessed."), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	public ResponseEntity<ApiResponse<List<String>>> handle(IllegalStateException e) {
		log.error("Illegal state", e);
		return ApiResponse.of(List.of("The request could not be processed in the current state."), HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ApiResponse<String>> handle(Exception e) {
		log.error("Unexpected system error", e);
		return ApiResponse.of("An unexpected error occurred. Please contact support if this persists.", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
