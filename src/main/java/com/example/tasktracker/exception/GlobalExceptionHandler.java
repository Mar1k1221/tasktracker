package com.example.tasktracker.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handlerDataIntegrityViolation(DataIntegrityViolationException dataIntegrityViolationException){
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Ошибка. Нарушение уникальности данных, возможно такой тег был использован до этого");

    }
}
