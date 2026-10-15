package com.fitness.model;

/**
 * Concrete subclass representing a Fitness Coach / Trainer.
 * Demonstrates:
 * - Inheritance from User
 * - Method Overriding (Polymorphism)
 * - Specialized fields (specialization, certification, experience, bio, rating)
 */
public class CoachUser extends User {
    private String specialization;
    private String certification;
    private int experienceYears;
    private double rating;
    private String bio;

    public CoachUser() {
        super();
        setRole(Role.COACH);
    }

    public CoachUser(Integer id, String name, String email, String passwordHash,
                     String specialization, String certification, int experienceYears,
                     double rating, String bio) {
        super(id, name, email, passwordHash, Role.COACH);
        this.specialization = specialization;
        this.certification = certification;
        this.experienceYears = experienceYears;
        this.rating = rating;
        this.bio = bio;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String getDashboardUrl() {
        return "/dashboard?view=coach";
    }

    @Override
    public String getProfileSummary() {
        return String.format("Coach [Spec: %s | Cert: %s | Exp: %d yrs | Rating: %.1f/5.0]",
                specialization, certification, experienceYears, rating);
    }
}
