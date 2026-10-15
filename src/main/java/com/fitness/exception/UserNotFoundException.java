package com.fitness.exception;

/**
 * Thrown when a user ID or email lookup yields no result.
 */
public class UserNotFoundException extends FitnessPlatformException {
    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }

    public UserNotFoundException(int userId) {
        super("USER_NOT_FOUND", "User with ID " + userId + " was not found in the platform.");
    }
}
