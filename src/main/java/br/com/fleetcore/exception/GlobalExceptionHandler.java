package br.com.fleetcore.exception;

import br.com.fleetcore.domain.vehicle.exception.BrandAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BrandNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBrandNotFound(
            BrandNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "BRAND_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(BrandAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBrandAlreadyExists(
            BrandAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "BRAND_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(BrandInactiveException.class)
    public ResponseEntity<ErrorResponse> handleBrandInactive(
            BrandInactiveException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "BRAND_INACTIVE",
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorResponse(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Validation failed",
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Invalid parameter value",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleObjectOptimisticLockingFailureException(
            ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request
    ){
        return buildResponse(
                HttpStatus.CONFLICT,
                "CONCURRENT_UPDATE",
                "This record has been updated by another user. Please reload the data and try again",
                request.getRequestURI(),
                null
                );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        if (isBrandNameUniquenessViolation(exception)) {
            return buildResponse(
                    HttpStatus.CONFLICT,
                    "BRAND_ALREADY_EXISTS",
                    "Brand already exists",
                    request.getRequestURI(),
                    null
            );
        }

        return buildResponse(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "The request could not be completed due to a data integrity constraint violation",
                request.getRequestURI(),
                null
        );
    }

    private boolean isBrandNameUniquenessViolation(DataIntegrityViolationException exception) {
        return exception.getCause() instanceof ConstraintViolationException cve
                && "uk_brand_name_lower".equals(cve.getConstraintName());
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            List<FieldErrorResponse> errors
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                path,
                errors
        );

        return ResponseEntity.status(status).body(response);
    }
}