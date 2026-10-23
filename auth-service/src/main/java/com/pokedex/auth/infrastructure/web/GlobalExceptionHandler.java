package com.pokedex.auth.infrastructure.web;

import com.pokedex.auth.domain.exception.DomainException;
import com.pokedex.auth.domain.exception.DomainValidationException;
import com.pokedex.auth.domain.exception.InvalidCredentialsException;
import com.pokedex.auth.domain.exception.OperationNotAllowedException;
import com.pokedex.auth.domain.exception.UserAlreadyExistsException;
import com.pokedex.auth.domain.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translates exceptions into RFC 7807 problem details.
 * Framework errors (malformed JSON, type mismatch, bean validation) are handled by the parent class (400).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Raised instead of MethodArgumentNotValidException when a handler has constraints on other parameters
     * too (e.g. {@code @Positive id} plus {@code @Valid} body), and for invalid path/query parameters.
     */
    @Override
    @SuppressWarnings("deprecation")
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                           HttpHeaders headers,
                                                                           HttpStatusCode status,
                                                                           WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getAllValidationResults().forEach(result -> {
            if (result instanceof ParameterErrors parameterErrors) {
                parameterErrors.getFieldErrors()
                        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
            } else {
                var name = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().stream().findFirst()
                        .ifPresent(error -> errors.putIfAbsent(name, error.getDefaultMessage()));
            }
        });
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        var problem = ProblemDetail.forStatusAndDetail(statusFor(ex), ex.getMessage());
        if (ex instanceof DomainValidationException validation) {
            problem.setProperty("errors", Map.of(validation.getField(), validation.getMessage()));
        }
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static HttpStatus statusFor(DomainException ex) {
        return switch (ex) {
            case DomainValidationException ignored -> HttpStatus.BAD_REQUEST;
            case UserNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case UserAlreadyExistsException ignored -> HttpStatus.CONFLICT;
            case OperationNotAllowedException ignored -> HttpStatus.CONFLICT;
            case InvalidCredentialsException ignored -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
