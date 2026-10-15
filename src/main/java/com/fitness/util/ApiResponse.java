package com.fitness.util;

import java.sql.Timestamp;

/**
 * Generic API response wrapper.
 * Demonstrates Java Generics with type parameter T.
 *
 * @param <T> payload type
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Timestamp timestamp;

    public ApiResponse() {
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
