package com.edu.authsystem.service;

/**
 * Service interface for email transmissions.
 * Handles registrations account verification links and password recovery requests.
 */
public interface EmailService {

    /**
     * Send account verification link to a newly registered user.
     *
     * @param email destination address
     * @param token verification token
     */
    void sendVerificationEmail(String email, String token);

    /**
     * Send password reset recovery link to a user.
     *
     * @param email destination address
     * @param token password reset token
     */
    void sendPasswordResetEmail(String email, String token);
}
