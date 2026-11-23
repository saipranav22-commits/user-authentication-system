package com.edu.authsystem;

import com.edu.authsystem.dto.AuthResponse;
import com.edu.authsystem.dto.LoginRequest;
import com.edu.authsystem.dto.RegisterRequest;
import com.edu.authsystem.dto.TokenRefreshRequest;
import com.edu.authsystem.dto.UserResponse;
import com.edu.authsystem.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

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

    private static String email = "portfolio.tester@example.com";
    private static String password = "StrongPassword123!";
    private static String verificationToken;
    private static String accessToken;
    private static String refreshToken;

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
}
