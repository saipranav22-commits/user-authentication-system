package com.edu.authsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Base abstract exception class for application-specific custom exceptions.
 * Allows custom exceptions to define their corresponding HTTP status codes.
 */
public abstract class CustomException extends RuntimeException {

    private final HttpStatus status;

    protected CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
