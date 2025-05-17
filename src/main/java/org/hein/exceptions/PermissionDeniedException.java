package org.hein.exceptions;

import lombok.Getter;

@Getter
public class PermissionDeniedException extends RuntimeException {
    private final String feature;
    private final String action;
    private final String role;

    public PermissionDeniedException(String message) {
        super(message);
        this.feature = null;
        this.action = null;
        this.role = null;
    }

    public PermissionDeniedException(String message, String feature, String action, String role) {
        super(message);
        this.feature = feature;
        this.action = action;
        this.role = role;
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
        this.feature = null;
        this.action = null;
        this.role = null;
    }
}
