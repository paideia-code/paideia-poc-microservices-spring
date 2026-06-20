package io.paideia.content.service.exception.handler;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import io.paideia.content.service.exception.custom.ContentAccessDeniedException;
import io.paideia.content.service.exception.custom.InvalidContentUploadException;
import io.paideia.content.service.exception.handler.ErrorResponseDTO.FieldErrorDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContentAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(ContentAccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidContentUploadException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidUpload(
            InvalidContentUploadException ex, WebRequest request) {
        return build(HttpStatus.valueOf(422), ex.getMessage(), request);
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

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestHeader(
            MissingRequestHeaderException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing required header: " + ex.getHeaderName(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestPart(
            MissingServletRequestPartException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing required multipart part: " + ex.getRequestPartName(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {
        return build(HttpStatus.valueOf(422), "Content file exceeds 5 MB limit", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request value: " + ex.getName(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(
                Instant.now(), status.value(), status.getReasonPhrase(), message, path(request)));
    }

    private ResponseEntity<ErrorResponseDTO> build(
            HttpStatus status, String message, WebRequest request, List<FieldErrorDTO> fieldErrors) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(
                Instant.now(), status.value(), status.getReasonPhrase(), message, path(request), fieldErrors));
    }

    private String path(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
