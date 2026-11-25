
package com.miniSprint.Digital_lending_loan_management_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RepaymentExceptionHandler {

    // Repayment not found
    @ExceptionHandler(RepaymentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRepaymentNotFound(
            RepaymentNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    // Invalid repayment operation
    @ExceptionHandler(InvalidRepaymentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRepayment(
            InvalidRepaymentException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    // Invalid input
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidArgument(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage()));
    }
}
