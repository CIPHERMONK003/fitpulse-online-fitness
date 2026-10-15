package com.fitness.model;

import java.sql.Timestamp;

/**
 * Model representing feedback and communication messages between a Coach and a Client.
 */
public class CoachFeedback implements Identifiable<Integer> {
    private Integer id;
    private Integer coachId;
    private String coachName;
    private Integer clientId;
    private String clientName;
    private String message;
    private String planRecommendation;
    private int ratingGiven; // 1-5
    private Timestamp createdAt;

    public CoachFeedback() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public CoachFeedback(Integer id, Integer coachId, String coachName, Integer clientId,
                         String clientName, String message, String planRecommendation, int ratingGiven) {
        this.id = id;
        this.coachId = coachId;
        this.coachName = coachName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.message = message;
        this.planRecommendation = planRecommendation;
        this.ratingGiven = ratingGiven;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCoachId() {
        return coachId;
    }

    public void setCoachId(Integer coachId) {
        this.coachId = coachId;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPlanRecommendation() {
        return planRecommendation;
    }

    public void setPlanRecommendation(String planRecommendation) {
        this.planRecommendation = planRecommendation;
    }

    public int getRatingGiven() {
        return ratingGiven;
    }

    public void setRatingGiven(int ratingGiven) {
        this.ratingGiven = ratingGiven;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("CoachFeedback[id=%d, coach='%s', client='%s', rating=%d, time=%s]",
                id, coachName, clientName, ratingGiven, createdAt);
    }
}
