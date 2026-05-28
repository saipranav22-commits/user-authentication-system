package com.edu.authsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to register with an email address
 * that is already registered in the system.
 * Maps to HTTP Status 409 Conflict.
 */
public class DuplicateEmailException extends CustomException {

    public DuplicateEmailException(String email) {
        super(String.format("Email '%s' is already registered.", email), HttpStatus.CONFLICT);
    }
}
