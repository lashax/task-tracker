package com.lasha.tasktracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
        log.error("API error: status={} message='{}' path={}",
                ex.getStatus().value(), ex.getMessage(), req.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                ex.getStatus(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthFailure(AuthenticationException ex, HttpServletRequest req) {
        log.error("Authentication failed: message='{}' path={}", ex.getMessage(), req.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.error("Access denied: message='{}' path={}", ex.getMessage(), req.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error at path={}", req.getRequestURI(), ex);

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error occurred",
                req.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
