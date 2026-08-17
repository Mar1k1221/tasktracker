package com.example.tasktracker.exception;

import com.example.tasktracker.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;


import java.time.LocalDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(TaskNotFoundException.class)
   public ResponseEntity<ApiError> exceptionTask(TaskNotFoundException taskNotFoundException,HttpServletRequest request){
        ApiError apiError = new ApiError("TASK_NOT_FOUND", taskNotFoundException.getMessage(),
               request.getRequestURI(), LocalDateTime.now());
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
   }
   @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflictException(ConflictException conflictException,HttpServletRequest request){
        ApiError apiError = new ApiError("CONFLICT_EXCEPTION",conflictException.getMessage(),request.getRequestURI(),LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
   }
   @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validationException(MethodArgumentNotValidException
                                                                   methodArgumentNotValidException,HttpServletRequest request){
        ApiError apiError = new ApiError("VALIDATION_EXCEPTION",methodArgumentNotValidException.getMessage(),request.getRequestURI(),LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
   }
   @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> exceptionOrJson(HttpMessageNotReadableException HttpMessageNotReadableException,
                                                    HttpServletRequest request){
        ApiError apiError = new ApiError("INVALID_JSON",HttpMessageNotReadableException.getMessage(),request.getRequestURI(),LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
   }
   @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiError> exceptionStatus(InvalidStatusTransitionException invalidStatusTransitionException,
                                                    HttpServletRequest request){
        ApiError apiError = new ApiError("INVALID_STATUS_TRANSITION",invalidStatusTransitionException.getMessage(),request.getRequestURI(),LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
   }
}
