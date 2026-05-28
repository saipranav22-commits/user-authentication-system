package com.edu.authsystem.controller;

import com.edu.authsystem.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Protected REST Controller containing routes accessible by authenticated Users.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * Endpoint returning profile data for the currently authenticated user.
     * GET /api/user/profile
     *
     * @return 200 OK containing user metadata, or 401/403 if unauthorized/forbidden
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile() {
        // Retrieve principal details from Spring Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (String) authentication.getPrincipal();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("email", userEmail);
        profileData.put("role", authentication.getAuthorities().toString());
        profileData.put("message", "Welcome to your secure user profile!");

        return new ResponseEntity<>(
                ApiResponse.success("Profile details fetched successfully.", profileData),
                HttpStatus.OK
        );
    }
}
