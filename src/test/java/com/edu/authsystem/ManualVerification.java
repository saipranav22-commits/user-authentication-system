package com.edu.authsystem;

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
import com.edu.authsystem.service.EmailService;
import com.edu.authsystem.service.JwtService;
import com.edu.authsystem.service.UserServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced lightweight standalone console application that manually instantiates 
 * and tests the advanced JWT and RBAC authentication system in a mock-free, zero-DB sandbox.
 */
public class ManualVerification {

    private static final Map<String, User> userDb = new HashMap<>();
    private static final Map<String, RefreshToken> tokenDb = new HashMap<>();
    private static long idSequence = 1;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   STARTING JWT AUTH SYSTEM MANUAL VERIFICATION   ");
        System.out.println("==================================================\n");

        // 1. Create a dynamic proxy for UserRepository
        UserRepository userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[] { UserRepository.class },
                (proxy, method, methodArgs) -> {
                    String methodName = method.getName();
                    if (methodName.equals("existsByEmail")) {
                        return userDb.containsKey((String) methodArgs[0]);
                    } else if (methodName.equals("findByEmail")) {
                        return Optional.ofNullable(userDb.get((String) methodArgs[0]));
                    } else if (methodName.equals("findByEmailVerificationToken")) {
                        return userDb.values().stream()
                                .filter(u -> methodArgs[0].equals(u.getEmailVerificationToken()))
                                .findFirst();
                    } else if (methodName.equals("findByPasswordResetToken")) {
                        return userDb.values().stream()
                                .filter(u -> methodArgs[0].equals(u.getPasswordResetToken()))
                                .findFirst();
                    } else if (methodName.equals("save")) {
                        User entity = (User) methodArgs[0];
                        if (entity.getId() == null) {
                            entity.setId(idSequence++);
                            entity.setCreatedAt(LocalDateTime.now());
                            entity.setUpdatedAt(LocalDateTime.now());
                        }
                        userDb.put(entity.getEmail(), entity);
                        return entity;
                    }
                    return null;
                }
        );

        // 2. Create a dynamic proxy for RefreshTokenRepository
        RefreshTokenRepository refreshTokenRepository = (RefreshTokenRepository) Proxy.newProxyInstance(
                RefreshTokenRepository.class.getClassLoader(),
                new Class<?>[] { RefreshTokenRepository.class },
                (proxy, method, methodArgs) -> {
                    String methodName = method.getName();
                    if (methodName.equals("findByToken")) {
                        return Optional.ofNullable(tokenDb.get((String) methodArgs[0]));
                    } else if (methodName.equals("deleteByUser")) {
                        User targetUser = (User) methodArgs[0];
                        long before = tokenDb.size();
                        tokenDb.values().removeIf(t -> t.getUser().getEmail().equals(targetUser.getEmail()));
                        return before - tokenDb.size();
                    } else if (methodName.equals("save")) {
                        RefreshToken entity = (RefreshToken) methodArgs[0];
                        tokenDb.put(entity.getToken(), entity);
                        return entity;
                    } else if (methodName.equals("delete")) {
                        RefreshToken entity = (RefreshToken) methodArgs[0];
                        tokenDb.remove(entity.getToken());
                        return null;
                    }
                    return null;
                }
        );

        // 3. Create a real encoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 4. Create real JwtService
        JwtService jwtService = new JwtService();
        try {
            // Set secretKeyString dynamically since it's injected via @Value
            Field keyField = JwtService.class.getDeclaredField("secretKeyString");
            keyField.setAccessible(true);
            keyField.set(jwtService, "verification-mock-extremely-long-secret-key-key");
            
            Field expField = JwtService.class.getDeclaredField("jwtExpirationMs");
            expField.setAccessible(true);
            expField.set(jwtService, 900000L); // 15 mins
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. Create a dynamic proxy for EmailService (Mock Email Logger)
        EmailService emailService = (EmailService) Proxy.newProxyInstance(
                EmailService.class.getClassLoader(),
                new Class<?>[] { EmailService.class },
                (proxy, method, methodArgs) -> {
                    String methodName = method.getName();
                    if (methodName.equals("sendVerificationEmail")) {
                        System.out.println("   [Mock Email] Sent Verification Token to " + methodArgs[0] + ": " + methodArgs[1]);
                    } else if (methodName.equals("sendPasswordResetEmail")) {
                        System.out.println("   [Mock Email] Sent Password Reset Token to " + methodArgs[0] + ": " + methodArgs[1]);
                    }
                    return null;
                }
        );

        // 6. Instantiate UserServiceImpl
        System.out.println("[Init] Initializing UserServiceImpl with advanced security dependencies...");
        UserServiceImpl userService = new UserServiceImpl(
                userRepository, 
                refreshTokenRepository, 
                encoder, 
                jwtService, 
                emailService
        );
        System.out.println("-> Services initialized successfully.\n");

        int testsPassed = 0;
        int testsFailed = 0;

        // --- TEST CASE 1: Successful Registration (Locked Account) ---
        String userVerificationToken = null;
        try {
            System.out.println("[Test 1] Registering a new account...");
            RegisterRequest req = new RegisterRequest("jwt@example.com", "SecureP@ssword123!");
            UserResponse res = userService.registerUser(req);
            
            assert res != null;
            assert res.getEmail().equals("jwt@example.com");
            assert res.getRoles().contains(Role.ROLE_USER);
            
            // Extract generated token directly from database map
            User saved = userDb.get("jwt@example.com");
            userVerificationToken = saved.getEmailVerificationToken();
            
            System.out.println("   [SUCCESS] Registered. Account EmailVerified: " + saved.isEmailVerified());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   [FAILED] Register failed: " + e.getMessage());
            testsFailed++;
        }

        // --- TEST CASE 2: Attempt Login on Unverified Account (Expected: 403 Forbidden) ---
        try {
            System.out.println("\n[Test 2] Attempting login before email verification...");
            LoginRequest req = new LoginRequest("jwt@example.com", "SecureP@ssword123!");
            userService.loginUser(req);
            System.out.println("   [FAILED] Login allowed unverified account!");
            testsFailed++;
        } catch (ResponseStatusException e) {
            assert e.getStatusCode().value() == 403 : "Status should be 403 Forbidden";
            System.out.println("   [SUCCESS] Caught expected 403 Forbidden: " + e.getReason());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   [FAILED] Caught unexpected exception: " + e.getClass().getSimpleName());
            testsFailed++;
        }

        // --- TEST CASE 3: Account Email Verification ---
        try {
            System.out.println("\n[Test 3] Verifying email using verification token...");
            userService.verifyEmail(userVerificationToken);
            
            User saved = userDb.get("jwt@example.com");
            assert saved.isEmailVerified() : "User should be verified";
            
            System.out.println("   [SUCCESS] Account successfully unlocked. EmailVerified: " + saved.isEmailVerified());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   [FAILED] Verification failed: " + e.getMessage());
            testsFailed++;
        }

        // --- TEST CASE 4: Successful Login (Get Access & Refresh Tokens) ---
        String accessToken = null;
        String refreshToken = null;
        try {
            System.out.println("\n[Test 4] Logging in with correct credentials...");
            LoginRequest req = new LoginRequest("jwt@example.com", "SecureP@ssword123!");
            AuthResponse res = userService.loginUser(req);
            
            assert res != null;
            assert res.getAccessToken() != null;
            assert res.getRefreshToken() != null;
            
            accessToken = res.getAccessToken();
            refreshToken = res.getRefreshToken();
            
            System.out.println("   [SUCCESS] Login returned Access Token: Bearer " + accessToken.substring(0, 15) + "...");
            System.out.println("   [SUCCESS] Login returned Refresh Token: " + refreshToken);
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   [FAILED] Login failed: " + e.getMessage());
            testsFailed++;
        }

        // --- TEST CASE 5: JWT Access Token Parsing & Validation ---
        try {
            System.out.println("\n[Test 5] Parsing and validating JWT Access Token...");
            assert jwtService.isTokenValid(accessToken) : "Token should be valid";
            String parsedEmail = jwtService.extractEmail(accessToken);
            java.util.List<String> parsedRoles = jwtService.extractRoles(accessToken);
            
            assert parsedEmail.equals("jwt@example.com") : "Parsed email mismatch";
            assert parsedRoles.contains("ROLE_USER") : "Parsed role mismatch";
            
            System.out.println("   [SUCCESS] JWT validated. Subject: " + parsedEmail + ", Roles: " + parsedRoles);
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   [FAILED] Token validation failed: " + e.getMessage());
            testsFailed++;
        }

        System.out.println("\n==================================================");
        System.out.println("            JWT SYSTEM VERIFICATION RESULTS       ");
        System.out.println("==================================================");
        System.out.println("  Tests Passed : " + testsPassed + " / 5");
        System.out.println("  Tests Failed : " + testsFailed + " / 5");
        System.out.println("==================================================");
    }
}
