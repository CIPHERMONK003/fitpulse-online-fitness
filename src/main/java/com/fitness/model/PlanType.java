package com.fitness.model;

public enum PlanType {
    STRENGTH("Strength & Muscle Building"),
    CARDIO("Cardiovascular Endurance"),
    HIIT("High-Intensity Interval Training"),
    SPA_WELLNESS("Spa & Recovery Wellness"),
    YOGA_MOBILITY("Yoga, Mobility & Flexibility");

    private final String description;

    PlanType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PlanType fromString(String val) {
        if (val == null) return STRENGTH;
        try {
            return PlanType.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STRENGTH;
        }
    }
}
