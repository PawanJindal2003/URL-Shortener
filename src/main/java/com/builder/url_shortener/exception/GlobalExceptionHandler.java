package com.builder.url_shortener.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.HtmlUtils;

import com.builder.url_shortener.config.MessageService;
import com.builder.url_shortener.config.Messages;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        String error = resolveErrorTitle(status);
        String message = messageService.get(ex.getMessageKey(), ex.getArgs());
        return buildResponse(status, error, message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                messageService.get(Messages.ERROR_TITLE_VALIDATION_FAILED),
                message,
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                messageService.get(Messages.ERROR_TITLE_VALIDATION_FAILED),
                message,
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                messageService.get(Messages.ERROR_TITLE_BAD_REQUEST),
                messageService.get(Messages.ERROR_MALFORMED_JSON),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error(messageService.get(Messages.LOG_EXCEPTION_UNEXPECTED, request.getRequestURI()), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                messageService.get(Messages.ERROR_TITLE_INTERNAL_SERVER_ERROR),
                messageService.get(Messages.ERROR_UNEXPECTED),
                request);
    }

    private String resolveErrorTitle(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> messageService.get(Messages.ERROR_TITLE_BAD_REQUEST);
            case NOT_FOUND -> messageService.get(Messages.ERROR_TITLE_NOT_FOUND);
            case GONE -> messageService.get(Messages.ERROR_TITLE_GONE);
            case INTERNAL_SERVER_ERROR -> messageService.get(Messages.ERROR_TITLE_INTERNAL_SERVER_ERROR);
            default -> status.getReasonPhrase();
        };
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {
        String safePath = HtmlUtils.htmlEscape(request.getRequestURI());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                safePath);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
