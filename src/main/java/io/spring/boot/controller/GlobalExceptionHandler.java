package io.spring.boot.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.spring.boot.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// Helper to reduce duplication
    private ResponseEntity<ErrorResponse> errorStatus(HttpStatus status, String... messages) {
        var response = new ErrorResponse(List.of(messages));
        return ResponseEntity.status(status).body(response);
    }
    
    // 422 Unprocessable Entity — validation / business rules
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException e) {
        return errorStatus(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    // 401 Unauthorized — bad login, missing/invalid token
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationError(AuthenticationException e) {
        return errorStatus(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    // 403 Forbidden — logged in, but not allowed (e.g. edit/delete other's subscription)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return errorStatus(HttpStatus.FORBIDDEN, "Access denied");
    }

    // 404 Not Found — user, subscription, payment not found
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
        return errorStatus(HttpStatus.NOT_FOUND, "Resource not found");
    }
    
    // 502 Bad Gateway — payment provider error
    @ExceptionHandler(PaymentProviderException.class)
    public ResponseEntity<ErrorResponse> handleStripeError(PaymentProviderException e) {
        return errorStatus(HttpStatus.BAD_GATEWAY, "Payment provider error. Please try again.");
    }
    
    // Optional: Catch-all for 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception e) {
        e.printStackTrace();
        return errorStatus(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
	
}
