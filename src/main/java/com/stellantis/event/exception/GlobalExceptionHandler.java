package com.stellantis.event.exception;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


@ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest req) {
        ErrorCode code = ex.getErrorCode();
        HttpStatus status = code.httpStatus;

        log.warn("API exception [{}] at {} - {}",
                code.name(), req.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(status)
                .body(apiError(code, ex.getMessage(), status, req));
    }

    // ---- Validation / Binding ----------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation error at {} - {}", req.getRequestURI(), message);

        return badRequest(ErrorCode.INVALID_STATUS, message, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest req) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("Constraint violation at {} - {}", req.getRequestURI(), message);

        // Use a generic invalid request code; override in controller/service if needed
        return badRequest(ErrorCode.INVALID_PAGE_SIZE, message, req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest req) {
        String message = "Missing required parameter: " + ex.getParameterName();
        log.warn("Missing parameter at {} - {}", req.getRequestURI(), message);
        return badRequest(ErrorCode.INVALID_STATUS, message, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {
        String message = "Invalid value for parameter '" + ex.getName() +
                "': " + ex.getValue() + " (required type: " + ex.getRequiredType().getSimpleName() + ")";
        log.warn("Type mismatch at {} - {}", req.getRequestURI(), message);
        return badRequest(ErrorCode.INVALID_STATUS, message, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex,
                                                      HttpServletRequest req) {
        String message = "Malformed request body";
        log.warn("Message not readable at {} - {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return badRequest(ErrorCode.INVALID_STATUS, message, req);
    }

    // ---- Security -----------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied at {} - {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(apiError(ErrorCode.FORBIDDEN, "Access denied", HttpStatus.FORBIDDEN, req));
    }

    // If Spring Security is present, AuthenticationException can be caught as well.
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(org.springframework.security.core.AuthenticationException ex,
                                               HttpServletRequest req) {
        log.warn("Unauthorized at {} - {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(apiError(ErrorCode.UNAUTHORIZED, "Authentication required", HttpStatus.UNAUTHORIZED, req));
    }

    // ---- Spring Web generic -------------------------------------------------

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        //String message = (Object)ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        log.warn("ErrorResponseException at {} - {} [{}]", req.getRequestURI(), "test", status.value());
        return ResponseEntity.status(status)
                .body(apiError(ErrorCode.INTERNAL_SERVER_ERROR, "test", status, req));
    }

    // ---- Fallback -----------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at " + req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiError(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, req));
    }

    // ---- Helpers ------------------------------------------------------------

    private ResponseEntity<ApiError> badRequest(ErrorCode code, String message, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(apiError(code, message, HttpStatus.BAD_REQUEST, req));
    }

    private ApiError apiError(ErrorCode code, String message, HttpStatus status, HttpServletRequest req) {
        return new ApiError(
                code.name(),
                message,
                status.value(),
                req.getRequestURI(),
                OffsetDateTime.now()
        );
    }

}
