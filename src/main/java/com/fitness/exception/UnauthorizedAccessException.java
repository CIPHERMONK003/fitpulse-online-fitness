package com.fitness.exception;

/**
 * Unchecked exception thrown when a user attempts to access an unauthorized resource or endpoint.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
