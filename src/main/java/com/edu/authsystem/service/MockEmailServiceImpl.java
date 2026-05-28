package com.edu.authsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of EmailService that logs link details directly to the console.
 * Enables zero-setup, offline testing of accounts and password reset tokens.
 */
@Service
public class MockEmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    @Value("${server.port:8090}")
    private String serverPort;

    @Override
    public void sendVerificationEmail(String email, String token) {
        String verificationUrl = String.format("http://localhost:%s/api/auth/verify-email?token=%s", serverPort, token);
        
        StringBuilder box = new StringBuilder();
        box.append("\n┌────────────────────────────────────────────────────────────────────────┐\n");
        box.append("│                         MOCK EMAIL: VERIFICATION                       │\n");
        box.append("├────────────────────────────────────────────────────────────────────────┤\n");
        box.append(String.format("│ TO:      %-61s │\n", email));
        box.append("│ SUBJECT: Verify Your Authentication Account                            │\n");
        box.append("│ MESSAGE: Welcome! Click the link below to verify your email address:   │\n");
        box.append(String.format("│ LINK:    %-61s │\n", verificationUrl));
        box.append("└────────────────────────────────────────────────────────────────────────┘\n");

        // Print using logger and Standard System Out to ensure visibility during command line executions
        logger.info(box.toString());
        System.out.println(box.toString());
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String resetUrl = String.format("http://localhost:%s/api/auth/password-reset/confirm?token=%s", serverPort, token);

        StringBuilder box = new StringBuilder();
        box.append("\n┌────────────────────────────────────────────────────────────────────────┐\n");
        box.append("│                        MOCK EMAIL: PASSWORD RESET                      │\n");
        box.append("├────────────────────────────────────────────────────────────────────────┤\n");
        box.append(String.format("│ TO:      %-61s │\n", email));
        box.append("│ SUBJECT: Reset Your Authentication Password                            │\n");
        box.append("│ MESSAGE: We received a password reset request. Click the link below:   │\n");
        box.append(String.format("│ LINK:    %-61s │\n", resetUrl));
        box.append("└────────────────────────────────────────────────────────────────────────┘\n");

        logger.info(box.toString());
        System.out.println(box.toString());
    }
}
