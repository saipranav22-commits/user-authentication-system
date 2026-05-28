package com.edu.authsystem.repository;

import com.edu.authsystem.entity.RefreshToken;
import com.edu.authsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for RefreshToken database transactions.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Locate a refresh token in the database.
     *
     * @param token refresh token string
     * @return Optional enclosing the RefreshToken record
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Delete an active refresh token associated with a User.
     * Used upon logout to invalidate active sessions.
     *
     * @param user target User record
     * @return count of deleted records
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    long deleteByUser(User user);
}
