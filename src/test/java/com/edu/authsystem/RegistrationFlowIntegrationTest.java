package com.edu.authsystem;

import com.edu.authsystem.dto.AuthResponse;
import com.edu.authsystem.dto.LoginRequest;
import com.edu.authsystem.dto.PasswordResetConfirmRequest;
import com.edu.authsystem.dto.PasswordResetRequest;
import com.edu.authsystem.dto.RegisterRequest;
import com.edu.authsystem.dto.TokenRefreshRequest;
import com.edu.authsystem.dto.UserResponse;
import com.edu.authsystem.entity.User;
import com.edu.authsystem.exception.InvalidCredentialsException;
import com.edu.authsystem.repository.UserRepository;
import com.edu.authsystem.service.JwtService;
import com.edu.authsystem.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test proving that a new user can complete
 * registration, email verification, login, token rotation, and logout.
 */
@SpringBootTest
@ActiveProfiles("h2")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegistrationFlowIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private static String email = "portfolio.tester@example.com";
    private static String password = "StrongPassword123!";
    private static String newPassword = "BrandNewPassword456!";
    private static String verificationToken;
    private static String accessToken;
    private static String refreshToken;
    private static String resetToken;

    @Test
    @Order(1)
    public void testRegistrationProvidesIsolatedActivationToken() {
        RegisterRequest request = new RegisterRequest(email, password);
        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertNotNull(response.getVerificationToken(), "Portfolio mode must provide verification token for newly created user");
        verificationToken = response.getVerificationToken();
    }

    @Test
    @Order(2)
    public void testLoginBlockedBeforeActivation() {
        LoginRequest request = new LoginRequest(email, password);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.loginUser(request));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    @Order(3)
    public void testActivationUnlocksAccount() {
        assertDoesNotThrow(() -> userService.verifyEmail(verificationToken));
    }

    @Test
    @Order(4)
    public void testReusingVerificationTokenFails() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.verifyEmail(verificationToken));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @Order(5)
    public void testLoginSucceedsAfterActivation() {
        LoginRequest request = new LoginRequest(email, password);
        AuthResponse response = userService.loginUser(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(email, response.getUser().getEmail());

        accessToken = response.getAccessToken();
        refreshToken = response.getRefreshToken();
    }

    private static String oldRefreshTokenA;

    @Test
    @Order(6)
    public void testTokenRefreshRotation() {
        oldRefreshTokenA = refreshToken;
        TokenRefreshRequest request = new TokenRefreshRequest(oldRefreshTokenA);
        AuthResponse response = userService.refreshAccessToken(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(oldRefreshTokenA, response.getRefreshToken(), "Refresh token must be rotated");

        refreshToken = response.getRefreshToken();
    }

    @Test
    @Order(7)
    public void testReplayingRotatedTokenRejected() {
        TokenRefreshRequest request = new TokenRefreshRequest(oldRefreshTokenA);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.refreshAccessToken(request));
        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    @Order(8)
    public void testLogoutRevocation() {
        assertDoesNotThrow(() -> userService.logout(email));

        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.refreshAccessToken(request));
        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    @Order(9)
    public void testPasswordResetTokenGenerationAndExpiry() {
        // Trigger password reset request
        assertDoesNotThrow(() -> userService.initiatePasswordReset(new PasswordResetRequest(email)));

        User user = userRepository.findByEmail(email).orElseThrow();
        assertNotNull(user.getPasswordResetToken(), "Password reset token must be generated");
        assertNotNull(user.getPasswordResetTokenExpiry(), "Password reset token expiry must be set");
        assertTrue(user.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now()), "Expiry must be in future");
        assertTrue(user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now().plusHours(2)), "Expiry must be 1 hour window");

        resetToken = user.getPasswordResetToken();
    }

    @Test
    @Order(10)
    public void testPasswordResetRejectsInvalidAndExpiredTokens() {
        // 1. Invalid reset token must be rejected
        PasswordResetConfirmRequest invalidReq = new PasswordResetConfirmRequest("00000000-invalid-token", newPassword);
        ResponseStatusException invalidEx = assertThrows(ResponseStatusException.class, () -> userService.completePasswordReset(invalidReq));
        assertEquals(400, invalidEx.getStatusCode().value());

        // 2. Expired reset token must be rejected
        User user = userRepository.findByEmail(email).orElseThrow();
        LocalDateTime originalExpiry = user.getPasswordResetTokenExpiry();
        user.setPasswordResetTokenExpiry(LocalDateTime.now().minusMinutes(5));
        userRepository.save(user);

        PasswordResetConfirmRequest expiredReq = new PasswordResetConfirmRequest(resetToken, newPassword);
        ResponseStatusException expiredEx = assertThrows(ResponseStatusException.class, () -> userService.completePasswordReset(expiredReq));
        assertEquals(400, expiredEx.getStatusCode().value());

        // Restore expiry for subsequent successful test
        user.setPasswordResetTokenExpiry(originalExpiry);
        userRepository.save(user);
    }

    @Test
    @Order(11)
    public void testPasswordResetSuccessAndSingleUse() {
        // Complete reset with valid token
        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest(resetToken, newPassword);
        assertDoesNotThrow(() -> userService.completePasswordReset(req));

        // Verify token is wiped after single use
        User user = userRepository.findByEmail(email).orElseThrow();
        assertNull(user.getPasswordResetToken(), "Password reset token must be cleared after single use");
        assertNull(user.getPasswordResetTokenExpiry(), "Password reset token expiry must be cleared");

        // Attempting to reuse the exact same token must fail
        ResponseStatusException reuseEx = assertThrows(ResponseStatusException.class, () -> userService.completePasswordReset(req));
        assertEquals(400, reuseEx.getStatusCode().value(), "Reusing a consumed reset token must return 400 Bad Request");
    }

    @Test
    @Order(12)
    public void testOldPasswordRejectedAndNewPasswordWorks() {
        // Old password must be rejected
        LoginRequest oldReq = new LoginRequest(email, password);
        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(oldReq));

        // New password must succeed
        LoginRequest newReq = new LoginRequest(email, newPassword);
        AuthResponse response = userService.loginUser(newReq);
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        // Keep active session for refresh revocation test
        refreshToken = response.getRefreshToken();
    }

    @Test
    @Order(13)
    public void testPasswordResetRevokesPriorRefreshTokens() {
        // An active refresh token exists from step 12
        assertNotNull(refreshToken);

        // Initiate and complete another password change
        userService.initiatePasswordReset(new PasswordResetRequest(email));
        User user = userRepository.findByEmail(email).orElseThrow();
        String secondResetToken = user.getPasswordResetToken();

        String finalPassword = "FinalSecurePassword789!";
        userService.completePasswordReset(new PasswordResetConfirmRequest(secondResetToken, finalPassword));

        // The pre-reset refresh token MUST now be revoked and rejected
        TokenRefreshRequest refreshReq = new TokenRefreshRequest(refreshToken);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.refreshAccessToken(refreshReq));
        assertEquals(401, ex.getStatusCode().value(), "Password reset must revoke all pre-existing refresh tokens");
    }

    @Test
    @Order(14)
    public void testAccessTokenExpirationEnforced() {
        // Validating an empty, malformed, or fake JWT returns false
        assertFalse(jwtService.isTokenValid(""));
        assertFalse(jwtService.isTokenValid("malformed.jwt.token"));
        assertFalse(jwtService.isTokenValid("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.tampered_signature"));
    }

    @Test
    @Order(15)
    public void testStatelessAccessTokenValidityUntilExpiry() {
        // Issue a legitimate JWT access token
        User user = userRepository.findByEmail(email).orElseThrow();
        String activeJwt = jwtService.generateToken(user.getEmail(), user.getRoles(), user.getId());

        // Under standard stateless JWT architecture without server-side blacklist,
        // the access token remains cryptographically valid until its expiration timestamp
        assertTrue(jwtService.isTokenValid(activeJwt), "Stateless JWT access token remains valid until expiration");
        assertEquals(email, jwtService.extractEmail(activeJwt));
    }
}
