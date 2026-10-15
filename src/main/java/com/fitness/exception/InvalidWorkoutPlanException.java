package com.fitness.exception;

/**
 * Thrown when validation of workout plan data fails.
 */
public class InvalidWorkoutPlanException extends FitnessPlatformException {
    public InvalidWorkoutPlanException(String message) {
        super("INVALID_PLAN", message);
    }
}
