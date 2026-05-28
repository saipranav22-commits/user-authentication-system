package com.edu.authsystem.dto;

import com.edu.authsystem.entity.Role;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object representing user details returned to clients.
 * Filters out sensitive credentials and exposes assigned security roles.
 */
public class UserResponse {

    private Long id;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Role> roles = new HashSet<>();
    private String token; // Access Token placeholder or actual value

    // Constructors
    public UserResponse() {
    }

    public UserResponse(Long id, String email, LocalDateTime createdAt, LocalDateTime updatedAt, Set<Role> roles) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
        this.token = "JWT_TOKEN_PLACEHOLDER";
    }

    public UserResponse(Long id, String email, LocalDateTime createdAt, LocalDateTime updatedAt, Set<Role> roles, String token) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
        this.token = token;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
