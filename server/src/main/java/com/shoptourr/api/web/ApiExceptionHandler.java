package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.common.CommonDtos.FieldErrorDto;
import com.shoptourr.domain.ApiException;
import com.shoptourr.domain.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final MediaType PROBLEM = MediaType.parseMediaType("application/problem+json");

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> handleApi(ApiException ex, HttpServletRequest request) {
        return problem(ex.status(), ex.code(), ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDto> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldErrorDto(
                        err.getField(),
                        err.getCode() == null ? "INVALID" : err.getCode(),
                        err.getDefaultMessage()))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Validation failed", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldErrorDto> errors = ex.getConstraintViolations().stream()
                .map(v -> new FieldErrorDto(v.getPropertyPath().toString(), "INVALID", v.getMessage()))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Validation failed", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Malformed request body", request, List.of());
    }

    @ExceptionHandler({AuthenticationException.class, JwtException.class})
    ResponseEntity<ProblemDetail> handleAuth(RuntimeException ex, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Unauthorized", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL, "Internal server error", request, List.of());
    }

    private static ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            ErrorCode code,
            String detail,
            HttpServletRequest request,
            List<FieldErrorDto> errors
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setType(URI.create("https://api.shoptourr.com/problems/" + code.name().toLowerCase().replace('_', '-')));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("code", code.name());
        pd.setProperty("errors", errors);
        pd.setProperty("requestId", RequestIdFilter.current());
        return ResponseEntity.status(status).contentType(PROBLEM).body(pd);
    }
}
