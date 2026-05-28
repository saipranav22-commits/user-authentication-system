package com.edu.authsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object containing parameters to finalize a password override.
 * Applies the same strict password strength constraints defined in RegisterRequest.
 */
public class PasswordResetConfirmRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_]).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!_)"
    )
    private String newPassword;

    // Constructors
    public PasswordResetConfirmRequest() {
    }

    public PasswordResetConfirmRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
