package com.example.productservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseError.builder()
                .timestamp(LocalDateTime.now()).status(404).error("Not Found").message(ex.getMessage()).build());
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ApiResponseError> handleOutOfStock(OutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseError.builder()
                .timestamp(LocalDateTime.now()).status(409).error("Conflict").message(ex.getMessage()).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse(ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponseError.builder()
                .timestamp(LocalDateTime.now()).status(400).error("Bad Request").message(msg).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseError> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseError.builder()
                .timestamp(LocalDateTime.now()).status(500).error("Internal Server Error").message(ex.getMessage()).build());
    }
}
