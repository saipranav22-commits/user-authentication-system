package com.edu.authsystem.service;

import com.edu.authsystem.dto.AuthResponse;
import com.edu.authsystem.dto.LoginRequest;
import com.edu.authsystem.dto.RegisterRequest;
import com.edu.authsystem.dto.UserResponse;
import com.edu.authsystem.entity.RefreshToken;
import com.edu.authsystem.entity.Role;
import com.edu.authsystem.entity.User;
import com.edu.authsystem.exception.DuplicateEmailException;
import com.edu.authsystem.exception.InvalidCredentialsException;
import com.edu.authsystem.exception.UserNotFoundException;
import com.edu.authsystem.repository.RefreshTokenRepository;
import com.edu.authsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl.
 * Verifies core security principles and business logic flow under the enhanced JWT architecture.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "SecureP@ss123!");
        loginRequest = new LoginRequest("test@example.com", "SecureP@ss123!");
        user = new User("test@example.com", "hashed_password");
        user.setId(1L);
    }

    @Test
    public void registerUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse response = userService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("test@example.com"), anyString());
    }

    @Test
    public void registerUser_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void loginUser_Success() {
        // Arrange
        user.setEmailVerified(true); // Account must be verified to log in
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anySet(), anyLong())).thenReturn("mock_access_token");

        // Act
        AuthResponse response = userService.loginUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(user.getEmail(), response.getUser().getEmail());
        assertEquals("mock_access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    public void loginUser_NotVerified_ThrowsException() {
        // Arrange
        user.setEmailVerified(false); // Verification locked!
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, 
                () -> userService.loginUser(loginRequest)
        );
        assertEquals(403, exception.getStatusCode().value());
        verify(jwtService, never()).generateToken(anyString(), anySet(), anyLong());
    }

    @Test
    public void loginUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.loginUser(loginRequest));
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void loginUser_InvalidCredentials_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequest));
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
    }
}
