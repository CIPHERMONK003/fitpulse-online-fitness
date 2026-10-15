package com.fitness.model;

/**
 * Interface defining contract for workout calorie calculation.
 * Demonstrates Java Interface with default method.
 */
@FunctionalInterface
public interface WorkoutCalculable {
    /**
     * Calculates estimated calories burned based on workout duration and user weight.
     *
     * @param durationMinutes duration in minutes
     * @param userWeightKg user body weight in kilograms
     * @return estimated calories burned
     */
    double calculateCaloriesBurned(int durationMinutes, double userWeightKg);

    /**
     * Default method to calculate MET (Metabolic Equivalent of Task) score.
     */
    default double calculateMET(int durationMinutes, double caloriesBurned, double userWeightKg) {
        if (durationMinutes <= 0 || userWeightKg <= 0) return 0.0;
        return (caloriesBurned / ((durationMinutes / 60.0) * userWeightKg));
    }
}
