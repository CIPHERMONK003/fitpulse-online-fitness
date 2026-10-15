package com.fitness.model;

public enum DifficultyLevel {
    BEGINNER("Beginner", 1.0),
    INTERMEDIATE("Intermediate", 1.25),
    ADVANCED("Advanced", 1.5);

    private final String displayName;
    private final double intensityMultiplier;

    DifficultyLevel(String displayName, double intensityMultiplier) {
        this.displayName = displayName;
        this.intensityMultiplier = intensityMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getIntensityMultiplier() {
        return intensityMultiplier;
    }

    public static DifficultyLevel fromString(String val) {
        if (val == null) return BEGINNER;
        try {
            return DifficultyLevel.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return BEGINNER;
        }
    }
}
