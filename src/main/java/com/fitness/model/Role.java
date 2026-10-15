package com.fitness.model;

public enum Role {
    CLIENT,
    COACH,
    ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return CLIENT;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CLIENT;
        }
    }
}
