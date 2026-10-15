package com.fitness.model;

/**
 * Concrete WorkoutPlan subclass for Strength / Hypertrophy Training.
 * Demonstrates:
 * - Inheritance from WorkoutPlan
 * - Polymorphic implementation of calculateCaloriesBurned()
 * - Specialized fields: targetMuscleGroup, sets, reps, restSec
 */
public class StrengthWorkoutPlan extends WorkoutPlan {
    private String targetMuscleGroup;
    private int recommendedSets;
    private int recommendedReps;
    private int restBetweenSetsSec;

    public StrengthWorkoutPlan() {
        super();
        setPlanType(PlanType.STRENGTH);
    }

    public StrengthWorkoutPlan(Integer id, Integer coachId, String coachName, String title,
                               String description, DifficultyLevel difficultyLevel, int durationWeeks,
                               int estimatedDurationMinutes, double baseCaloriesBurned,
                               String targetMuscleGroup, int recommendedSets, int recommendedReps,
                               int restBetweenSetsSec) {
        super(id, coachId, coachName, title, description, PlanType.STRENGTH, difficultyLevel,
                durationWeeks, estimatedDurationMinutes, baseCaloriesBurned);
        this.targetMuscleGroup = targetMuscleGroup;
        this.recommendedSets = recommendedSets;
        this.recommendedReps = recommendedReps;
        this.restBetweenSetsSec = restBetweenSetsSec;
    }

    public String getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    public void setTargetMuscleGroup(String targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }

    public int getRecommendedSets() {
        return recommendedSets;
    }

    public void setRecommendedSets(int recommendedSets) {
        this.recommendedSets = recommendedSets;
    }

    public int getRecommendedReps() {
        return recommendedReps;
    }

    public void setRecommendedReps(int recommendedReps) {
        this.recommendedReps = recommendedReps;
    }

    public int getRestBetweenSetsSec() {
        return restBetweenSetsSec;
    }

    public void setRestBetweenSetsSec(int restBetweenSetsSec) {
        this.restBetweenSetsSec = restBetweenSetsSec;
    }

    /**
     * Polymorphic calorie calculation tailored for resistance training:
     * Strength training burns calories based on muscle activation, sets/reps load,
     * and difficulty multiplier.
     */
    @Override
    public double calculateCaloriesBurned(int durationMinutes, double userWeightKg) {
        if (durationMinutes <= 0 || userWeightKg <= 0) return 0.0;
        // Base MET for weight training is ~5.0 - 6.0
        double baseMet = 5.5 * getDifficultyLevel().getIntensityMultiplier();
        // Muscle volume modifier: compound groups burn ~15% more
        double muscleModifier = (targetMuscleGroup != null && (targetMuscleGroup.toLowerCase().contains("leg") || targetMuscleGroup.toLowerCase().contains("full"))) ? 1.15 : 1.0;
        // Formula: Calories = MET * 3.5 * weightKg / 200 * durationMinutes * muscleModifier
        return Math.round((baseMet * 3.5 * userWeightKg / 200.0) * durationMinutes * muscleModifier);
    }

    @Override
    public String generateWorkoutSummary() {
        return String.format("Strength Plan: Target [%s] | %d Sets x %d Reps (Rest: %ds)",
                targetMuscleGroup, recommendedSets, recommendedReps, restBetweenSetsSec);
    }

    @Override
    public String getSpecificAttributeLabel() {
        return "Target Muscle / Volume";
    }

    @Override
    public String getSpecificAttributeValue() {
        return String.format("%s (%d sets x %d reps, %ds rest)", targetMuscleGroup, recommendedSets, recommendedReps, restBetweenSetsSec);
    }
}
