package com.edu.authsystem.repository;

import com.edu.authsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User operations.
 * Enhanced with token lookup methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Look up a user by their email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Locate a user record by their email verification token.
     *
     * @param token verification token
     * @return Optional containing the User if found, or empty otherwise
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Locate a user record by their password reset token.
     *
     * @param token password reset token
     * @return Optional containing the User if found, or empty otherwise
     */
    Optional<User> findByPasswordResetToken(String token);
}
