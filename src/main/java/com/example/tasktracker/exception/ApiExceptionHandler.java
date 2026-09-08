package com.example.tasktracker.exception;

import com.example.tasktracker.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


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
    public ResponseEntity<ApiError> validationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String errorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ApiError apiError = new ApiError("VALIDATION_EXCEPTION", errorMessage, request.getRequestURI(), LocalDateTime.now());
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
   }@ExceptionHandler(DataIntegrityViolationException.class)
   public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception,HttpServletRequest request){
        ApiError apiError = new ApiError("CONSTRAINT_VIOLATION","Запись нарушает ограничения базы данных",request.getRequestURI(),LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
   }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String message = String.format("Параметр '%s' должен иметь тип '%s'",
                exception.getName(),
                exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "неизвестный");
        ApiError apiError = new ApiError("TYPE_MISMATCH", message, request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllUncaughtException(Exception exception, HttpServletRequest request) {
        ApiError apiError = new ApiError("INTERNAL_SERVER_ERROR", "Внутренняя ошибка сервера", request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
