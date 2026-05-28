package com.edu.authsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails due to incorrect credentials.
 * Maps to HTTP Status 401 Unauthorized.
 * Keep message generic for secure authentication practices.
 */
public class InvalidCredentialsException extends CustomException {

    public InvalidCredentialsException() {
        super("Invalid email or password.", HttpStatus.UNAUTHORIZED);
    }
}
