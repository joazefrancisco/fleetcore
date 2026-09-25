package br.com.fleetcore.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnBrandAlreadyExists_WhenConstraintIsBrandNameUniqueness() {
        when(request.getRequestURI()).thenReturn("/brands");

        DataIntegrityViolationException exception = buildExceptionWithConstraint("uk_brand_name_lower");

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolationException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("BRAND_ALREADY_EXISTS", response.getBody().error());
        assertEquals("Brand already exists", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnDataIntegrityViolation_WhenConstraintIsDifferent() {
        when(request.getRequestURI()).thenReturn("/models");

        DataIntegrityViolationException exception = buildExceptionWithConstraint("fk_model_brand");

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolationException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().error());
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnDataIntegrityViolation_WhenCauseIsNotConstraintViolationException() {
        when(request.getRequestURI()).thenReturn("/brands");

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("unexpected failure", new RuntimeException("root cause"));

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolationException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().error());
    }

    private DataIntegrityViolationException buildExceptionWithConstraint(String constraintName) {
        ConstraintViolationException hibernateCause = new ConstraintViolationException(
                "duplicate key value violates unique constraint \"" + constraintName + "\"",
                new SQLException("duplicate key value violates unique constraint \"" + constraintName + "\""),
                constraintName
        );

        return new DataIntegrityViolationException("could not execute statement", hibernateCause);
    }
}