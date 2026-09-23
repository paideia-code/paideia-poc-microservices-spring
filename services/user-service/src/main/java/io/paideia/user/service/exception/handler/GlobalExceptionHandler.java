package io.paideia.user.service.exception.handler;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.paideia.user.service.exception.custom.UserNotFoundException;
import io.paideia.user.service.exception.handler.ErrorResponseDTO.FieldErrorDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(UserNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldErrorDTO> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorDTO(e.getField(), e.getDefaultMessage()))
                .toList();
        return build(HttpStatus.valueOf(422), "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadable(HttpMessageNotReadableException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, WebRequest request) {
        log.error("event=user.unexpected_error path={}", getPath(request), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(
                Instant.now(), status.value(), status.getReasonPhrase(), message, getPath(request)));
    }

    private ResponseEntity<ErrorResponseDTO> build(
            HttpStatus status, String message, WebRequest request, List<FieldErrorDTO> fieldErrors) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(
                Instant.now(), status.value(), status.getReasonPhrase(), message, getPath(request), fieldErrors));
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
