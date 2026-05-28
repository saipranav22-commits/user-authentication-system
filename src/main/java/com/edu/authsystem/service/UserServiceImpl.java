package com.edu.authsystem.service;

import com.edu.authsystem.dto.AuthResponse;
import com.edu.authsystem.dto.LoginRequest;
import com.edu.authsystem.dto.PasswordResetConfirmRequest;
import com.edu.authsystem.dto.PasswordResetRequest;
import com.edu.authsystem.dto.RegisterRequest;
import com.edu.authsystem.dto.TokenRefreshRequest;
import com.edu.authsystem.dto.UserResponse;
import com.edu.authsystem.entity.RefreshToken;
import com.edu.authsystem.entity.Role;
import com.edu.authsystem.entity.User;
import com.edu.authsystem.exception.DuplicateEmailException;
import com.edu.authsystem.exception.InvalidCredentialsException;
import com.edu.authsystem.exception.UserNotFoundException;
import com.edu.authsystem.repository.RefreshTokenRepository;
import com.edu.authsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation managing user registration, authentication, JWT tokens,
 * Refresh Token rotation, password overrides, and email verification processes.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        // 1. Prevent duplicate emails
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 2. Hash raw password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Instantiate user with default role (ROLE_USER)
        User user = new User(request.getEmail(), hashedPassword);
        user.setEmailVerified(false); // Locked by default until verified

        // 4. Generate verification token (expires in 24 hours)
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        // 5. Trigger mock email transmission
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        return convertToResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse loginUser(LoginRequest request) {
        // 1. Find user in repository
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // 2. Validate passwords
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 3. Enforce email verification rule
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, 
                    "Your email address has not been verified yet. Please check your inbox."
            );
        }

        // 4. Generate dynamic short-lived JWT Access Token
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRoles(), user.getId());

        // 5. Generate secure longer-lived Refresh Token (expires in 30 days)
        // Clean out any existing active refresh tokens first to prevent session duplicates
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        
        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(
                user, 
                refreshTokenStr, 
                Instant.now().plus(Duration.ofDays(30))
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenStr, convertToResponse(user));
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // Find user by verification token
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        "Invalid or expired verification token."
                ));

        // Enforce verification expiration checks
        if (LocalDateTime.now().isAfter(user.getEmailVerificationTokenExpiry())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Verification token has expired. Please request a new registration."
            );
        }

        // Lock verification status to true and clear token
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(TokenRefreshRequest request) {
        // 1. Find active refresh token
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, 
                        "Invalid refresh token."
                ));

        // 2. Verify token expiration
        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, 
                    "Refresh token has expired. Please sign in again."
            );
        }

        User user = oldToken.getUser();

        // 3. Generate new short-lived JWT Access Token
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRoles(), user.getId());

        // 4. Rotate Refresh Token (Dynamic Token Rotation best-practice)
        refreshTokenRepository.delete(oldToken);
        refreshTokenRepository.flush();
        String newRefreshTokenStr = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = new RefreshToken(
                user, 
                newRefreshTokenStr, 
                Instant.now().plus(Duration.ofDays(30))
        );
        refreshTokenRepository.save(newRefreshToken);

        return new AuthResponse(newAccessToken, newRefreshTokenStr, convertToResponse(user));
    }

    @Override
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        
        // Invalidate session by deleting refresh token
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(PasswordResetRequest request) {
        // Look up target account
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // Generate secure password reset token (expires in 1 hour)
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        // Trigger mock recovery email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Override
    @Transactional
    public void completePasswordReset(PasswordResetConfirmRequest request) {
        // Find user by reset token
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        "Invalid or expired password reset token."
                ));

        // Enforce token expiration checks
        if (LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Password reset token has expired. Please request a new link."
            );
        }

        // Apply new hashed password and clear recovery fields
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        userRepository.save(user);
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRoles()
        );
    }
}
