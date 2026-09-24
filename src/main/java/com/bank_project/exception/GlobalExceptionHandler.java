package com.bank_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.MethodArgumentNotValidException;



@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException exception) {

        Map<String, Object> body = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(
            InsufficientBalanceException exception) {

        Map<String, Object> body = Map.of(
                "status", HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "message", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        Map<String, Object> response = new HashMap<>();

        response.put("status", 400);
        response.put("message", "Validation error");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SameAccountTransferException.class)
    public ResponseEntity<Map<String, Object>> handleSameAccountTransferException(
              SameAccountTransferException ex) {

        Map<String, Object> sameResponse = new HashMap<>();

        sameResponse.put("status",400);
        sameResponse.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(sameResponse);

    }
    @ExceptionHandler(BlockedAccountException.class)
    public ResponseEntity<Map<String, Object>> handleBlockedAccountException(
            BlockedAccountException ex) {
        Map<String, Object> blocked = new HashMap<>();

        blocked.put("status",400);
        blocked.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(blocked);
    }

      @ExceptionHandler(InvalidLoanStatusException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidLoanStatusException(
              InvalidLoanStatusException ex) {

            Map<String, Object> loanException = new HashMap<>();

          loanException.put("status", 400);
          loanException.put("message", ex.getMessage());
          return ResponseEntity.badRequest().body(loanException);
      }


    @ExceptionHandler(AccountNumberAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNumberAlreadyExistsException(
            AccountNumberAlreadyExistsException ex) {

        Map<String, Object> numberUsed = new HashMap<>();

        numberUsed.put("status", 400);
        numberUsed.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(numberUsed);
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleCpfAlreadyExistsException(
            CpfAlreadyExistsException ex) {

        Map<String, Object> cpfUsed = new HashMap<>();

        cpfUsed.put("status", 400);
        cpfUsed.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(cpfUsed);
    }


}