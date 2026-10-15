package com.fitness.exception;

/**
 * Thrown when trying to register or create an account with an email already in use.
 */
public class UserAlreadyExistsException extends FitnessPlatformException {
    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", "A user account with email '" + email + "' already exists.");
    }
}
