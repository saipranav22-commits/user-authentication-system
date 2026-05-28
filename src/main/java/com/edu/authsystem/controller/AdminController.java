package com.edu.authsystem.controller;

import com.edu.authsystem.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Protected REST Controller containing routes restricted strictly to Administrators.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * Endpoint returning administrative dashboard statistics.
     * GET /api/admin/dashboard
     *
     * @return 200 OK containing stats, or 401/403 if unauthorized/forbidden
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> adminStats = new HashMap<>();
        adminStats.put("totalUsers", 450);
        adminStats.put("activeSessions", 125);
        adminStats.put("systemHealth", "100% Operational");
        adminStats.put("message", "Welcome, Administrator. Access granted to dashboard stats.");

        return new ResponseEntity<>(
                ApiResponse.success("Administrative dashboard statistics fetched successfully.", adminStats),
                HttpStatus.OK
        );
    }
}
