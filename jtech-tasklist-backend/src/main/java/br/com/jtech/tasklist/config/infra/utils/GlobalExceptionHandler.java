package br.com.jtech.tasklist.config.infra.handlers;

import br.com.jtech.tasklist.application.core.exceptions.InvalidCredentialsException;
import br.com.jtech.tasklist.config.infra.exceptions.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        error.setMessage("Error on request");
        error.setTimestamp(LocalDateTime.now());
        error.setSubErrors(subErrors(ex));
        error.setDebugMessage(ex.getLocalizedMessage());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (message.contains("uk_tasklist_user_name")) {
            error.setMessage("A list with this name already exists");
        } else if (message.contains("uk_users_email")) {
            error.setMessage("Email already registered");
        } else if (message.contains("uk_task_tasklist_title")) {
            error.setMessage("A task with this title already exists in this list");
        } else {
            error.setMessage("Data integrity violation");
        }

        error.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED);
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        ApiError error = new ApiError(HttpStatus.FORBIDDEN);
        error.setMessage("Access denied");
        error.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        error.setMessage("Malformed JSON request");
        error.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR);
        error.setMessage("Internal server error");
        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage(ex.getLocalizedMessage());
        return buildResponseEntity(error);
    }

    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    private List<ApiSubError> subErrors(MethodArgumentNotValidException ex) {
        List<ApiSubError> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ApiValidationError api = new ApiValidationError(
                ex.getObjectName(), fieldError.getField(),
                fieldError.getRejectedValue(), fieldError.getDefaultMessage());
            errors.add(api);
        }
        return errors;
    }
}
