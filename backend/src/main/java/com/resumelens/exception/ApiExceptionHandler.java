package com.resumelens.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> handle(ApiException error, HttpServletRequest request) {
        return ResponseEntity.status(error.status()).body(body(error.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, Object>> tooLarge(HttpServletRequest request) {
        return ResponseEntity.status(413).body(body("This resume exceeds the maximum permitted file size.", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(body("We could not complete the analysis. Please try another resume.", request.getRequestURI()));
    }

    private Map<String, Object> body(String message, String path) {
        return Map.of("timestamp", Instant.now().toString(), "status", "error", "message", message, "path", path);
    }
}
