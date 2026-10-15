package com.fitness.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing and verifying passwords securely using SHA-256.
 */
public final class PasswordUtil {

    private static final String SALT = "FitnessCoachingPlatform2026_SaltKey#99";

    private PasswordUtil() {}

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest((SALT + plainPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        return hashPassword(plainPassword).equalsIgnoreCase(hashedPassword);
    }
}
