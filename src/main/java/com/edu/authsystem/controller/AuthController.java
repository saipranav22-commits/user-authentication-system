package com.edu.authsystem.controller;

import com.edu.authsystem.dto.ApiResponse;
import com.edu.authsystem.dto.AuthResponse;
import com.edu.authsystem.dto.LoginRequest;
import com.edu.authsystem.dto.PasswordResetConfirmRequest;
import com.edu.authsystem.dto.PasswordResetRequest;
import com.edu.authsystem.dto.RegisterRequest;
import com.edu.authsystem.dto.TokenRefreshRequest;
import com.edu.authsystem.dto.UserResponse;
import com.edu.authsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST Controller exposing public endpoints for registration,
 * email verification, access refreshes, logouts, and password resets.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint for user registration.
     * POST /api/auth/register
     *
     * @param request Validated RegisterRequest payload
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        
        return new ResponseEntity<>(
                ApiResponse.success("User registered successfully. Please verify your email via the sent link.", response),
                HttpStatus.CREATED
        );
    }

    /**
     * Endpoint for user email verification.
     * GET /api/auth/verify-email
     *
     * @param token verification token string
     */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        
        return new ResponseEntity<>(
                ApiResponse.success("Email address verified successfully. You may now log in."),
                HttpStatus.OK
        );
    }

    /**
     * Endpoint for user login authentication.
     * POST /api/auth/login
     *
     * @param request Validated LoginRequest payload
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.loginUser(request);
        
        return new ResponseEntity<>(
                ApiResponse.success("Authentication successful.", response),
                HttpStatus.OK
        );
    }

    /**
     * Endpoint to exchange a valid refresh token for a new Access Token.
     * POST /api/auth/refresh
     *
     * @param request Validated TokenRefreshRequest payload
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = userService.refreshAccessToken(request);
        
        return new ResponseEntity<>(
                ApiResponse.success("Access Token refreshed successfully.", response),
                HttpStatus.OK
        );
    }

    /**
     * Endpoint for user logout (invalidates active refresh tokens).
     * POST /api/auth/logout
     * Requires active JWT Bearer authentication to determine context principal.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to perform logout.");
        }
        String userEmail = (String) authentication.getPrincipal();
        
        userService.logout(userEmail);
        SecurityContextHolder.clearContext();
        
        return new ResponseEntity<>(
                ApiResponse.success("Logout successful. Active session terminated."),
                HttpStatus.OK
        );
    }

    /**
     * Endpoint to request a password reset recovery link.
     * POST /api/auth/password-reset/request
     *
     * @param request Validated PasswordResetRequest payload
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        userService.initiatePasswordReset(request);
        
        return new ResponseEntity<>(
                ApiResponse.success("If the account exists, a password reset link has been dispatched."),
                HttpStatus.OK
        );
    }

    /**
     * Endpoint to complete a password override using a reset token.
     * POST /api/auth/password-reset/confirm
     *
     * @param request Validated PasswordResetConfirmRequest payload
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        userService.completePasswordReset(request);
        
        return new ResponseEntity<>(
                ApiResponse.success("Password reset completed successfully. You may now log in with your new password."),
                HttpStatus.OK
        );
    }
}
