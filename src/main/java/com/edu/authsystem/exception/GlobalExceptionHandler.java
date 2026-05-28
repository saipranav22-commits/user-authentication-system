package com.edu.authsystem.exception;

import com.edu.authsystem.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller advice class that captures all exceptions thrown from the controller layer.
 * Converts raw stack traces and Java exceptions into clean, structured ApiResponse payloads.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles custom domain-level exceptions (e.g., DuplicateEmailException, InvalidCredentialsException).
     * Extracts the designated HTTP status from the exception dynamically.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        logger.warn("Business rule violation: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Handles Spring ResponseStatusExceptions, preserving their defined status and message.
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(org.springframework.web.server.ResponseStatusException ex) {
        logger.warn("HTTP response status exception: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ex.getReason());
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    /**
     * Handles validation errors thrown when Spring validates DTO attributes (using @Valid).
     * Maps to HTTP Status 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Validation error on request processing: {} error(s) found.", ex.getBindingResult().getErrorCount());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error("Request validation failed.", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback handler for all uncaught, system-level exceptions.
     * Prevents leaking critical system internals (e.g., database details or stack traces) to external clients.
     * Maps to HTTP Status 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        // Log the complete trace internally for administrative debugging
        logger.error("An unexpected application error occurred:", ex);

        ApiResponse<Void> response = ApiResponse.error("An internal server error occurred. Please try again later.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
