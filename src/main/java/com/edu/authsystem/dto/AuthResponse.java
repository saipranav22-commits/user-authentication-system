package com.edu.authsystem.dto;

/**
 * Data Transfer Object wrapping successful authentication payloads.
 * Unifies access tokens, refresh tokens, and safe user profile metadata in one JSON structure.
 */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
