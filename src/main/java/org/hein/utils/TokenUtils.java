package org.hein.utils;

import org.hein.exceptions.ApiJwtTokenInvalidationException;
import org.springframework.util.StringUtils;

public class TokenUtils {
    private static final String BEARER_PREFIX = "Bearer ";

    public static String extractToken(String headerValue) {
        if (!StringUtils.hasText(headerValue) || !headerValue.startsWith(BEARER_PREFIX)) {
            throw new ApiJwtTokenInvalidationException("Invalid or missing Bearer token");
        }
        return headerValue.substring(BEARER_PREFIX.length());
    }
}
