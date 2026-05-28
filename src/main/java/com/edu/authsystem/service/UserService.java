package com.edu.authsystem.service;

import com.edu.authsystem.dto.AuthResponse;
import com.edu.authsystem.dto.LoginRequest;
import com.edu.authsystem.dto.PasswordResetConfirmRequest;
import com.edu.authsystem.dto.PasswordResetRequest;
import com.edu.authsystem.dto.RegisterRequest;
import com.edu.authsystem.dto.TokenRefreshRequest;
import com.edu.authsystem.dto.UserResponse;

/**
 * Service interface defining user transactions.
 * Handles account creations, email verifications, access refreshes, logouts, and password recoveries.
 */
public interface UserService {

    /**
     * Registers a new user.
     * Generates a time-limited email verification token and logs verification link.
     */
    UserResponse registerUser(RegisterRequest request);

    /**
     * Authenticates existing user.
     * Verifies that the email exists, password matches, and that the account is email-verified.
     * Generates access token (JWT) and persistent refresh token.
     */
    AuthResponse loginUser(LoginRequest request);

    /**
     * Validate verification token and unlock user account.
     */
    void verifyEmail(String token);

    /**
     * Exchange a valid refresh token for a brand new Access Token (JWT).
     */
    AuthResponse refreshAccessToken(TokenRefreshRequest request);

    /**
     * Delete active refresh tokens associated with the user.
     */
    void logout(String email);

    /**
     * Generates secure, time-limited reset recovery token and logs link.
     */
    void initiatePasswordReset(PasswordResetRequest request);

    /**
     * Validate password reset token, override old hash, and save.
     */
    void completePasswordReset(PasswordResetConfirmRequest request);
}
