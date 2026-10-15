package com.fitness.exception;

/**
 * Base custom checked exception for the Fitness Coaching Platform.
 * Demonstrates Java Exception Handling hierarchy.
 */
public class FitnessPlatformException extends Exception {
    private final String errorCode;

    public FitnessPlatformException(String message) {
        super(message);
        this.errorCode = "FITNESS_ERROR";
    }

    public FitnessPlatformException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FitnessPlatformException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FITNESS_ERROR";
    }

    public FitnessPlatformException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
