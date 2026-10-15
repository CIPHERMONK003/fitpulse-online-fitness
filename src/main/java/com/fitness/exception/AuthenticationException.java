package com.fitness.exception;

/**
 * Thrown on failed login or invalid credentials.
 */
public class AuthenticationException extends FitnessPlatformException {
    public AuthenticationException(String message) {
        super("AUTH_FAILED", message);
    }
}
