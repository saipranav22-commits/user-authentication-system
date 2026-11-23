package com.edu.authsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of EmailService.
 * Sanitized to ensure sensitive tokens and reset URLs are never leaked to logs or stdout.
 */
@Service
public class MockEmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    @Override
    public void sendVerificationEmail(String email, String token) {
        // Redact token details to prevent leaking authentication secrets into server logs
        logger.info("Verification dispatch triggered for recipient: {}. Action: Account Verification.", email);
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        // Redact reset token details to prevent unauthorized account takeover via server logs
        logger.info("Password recovery dispatch triggered for recipient: {}. Action: Password Reset.", email);
    }
}
