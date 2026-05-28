package com.edu.authsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user lookup fails in the database.
 * Maps to HTTP Status 404 Not Found.
 */
public class UserNotFoundException extends CustomException {

    public UserNotFoundException(String email) {
        super(String.format("User with email '%s' was not found.", email), HttpStatus.NOT_FOUND);
    }
}
