package com.ma.message_apps.exception;

/**
 * Exception thrown when a user attempts to access resources they're not authorized for
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
