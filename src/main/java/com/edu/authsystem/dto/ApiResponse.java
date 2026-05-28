package com.edu.authsystem.dto;

import java.time.LocalDateTime;

/**
 * Standardized API Response Wrapper.
 * Unifies successful responses and error details into a predictable JSON envelope.
 *
 * @param <T> Type of payload enclosed in the 'data' field
 */
public class ApiResponse<T> {

    private boolean status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    public ApiResponse(boolean status, String message, T data) {
        this();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Static helper methods for fluent construction
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> error(String message, T errors) {
        return new ApiResponse<>(false, message, errors);
    }

    // Getters and Setters
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
