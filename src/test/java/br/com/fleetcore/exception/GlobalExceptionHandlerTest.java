package br.com.fleetcore.exception;

import br.com.fleetcore.domain.vehicle.entity.Brand;
import br.com.fleetcore.domain.vehicle.exception.BrandAlreadyExistsException;
import br.com.fleetcore.domain.vehicle.exception.BrandInactiveException;
import br.com.fleetcore.domain.vehicle.exception.BrandNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleDataIntegrityViolationException_ShouldReturnBrandAlreadyExists_WhenConstraintIsBrandNameUniqueness() {
        DataIntegrityViolationException exception = buildExceptionWithConstraint("uk_brand_name_lower");

        when(request.getRequestURI()).thenReturn("/brands");

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolationException(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("BRAND_ALREADY_EXISTS", response.getBody().error());
        assertEquals("Brand already exists", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnDataIntegrityViolation_WhenConstraintIsDifferent() {
        DataIntegrityViolationException exception = buildExceptionWithConstraint("fk_model_brand");

        when(request.getRequestURI()).thenReturn("/models");

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolationException(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().error());
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnDataIntegrityViolation_WhenCauseIsNotConstraintViolationException() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("unexpected failure", new RuntimeException("root cause"));

        when(request.getRequestURI()).thenReturn("/brands");

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolationException(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().error());
    }

    @Test
    void handleBrandNotFound_ShouldReturnNotFound(){
        BrandNotFoundException exception = new BrandNotFoundException("Brand not found");

        when(request.getRequestURI()).thenReturn("/brands");

        ResponseEntity<ErrorResponse> response = handler.handleBrandNotFound(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("BRAND_NOT_FOUND", response.getBody().error());
        assertEquals("Brand not found", response.getBody().message());
    }

    @Test
    void handleBrandAlreadyExists_ShouldReturnConflict(){
        BrandAlreadyExistsException exception = new BrandAlreadyExistsException("Brand Already exists");

        when(request.getRequestURI()).thenReturn("/brands");

        ResponseEntity<ErrorResponse> response = handler.handleBrandAlreadyExists(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("BRAND_ALREADY_EXISTS", response.getBody().error());
        assertEquals("Brand Already exists", response.getBody().message());
    }

    @Test
    void handleBrandInactive_ShouldReturnConflict(){
        BrandInactiveException exception = new BrandInactiveException("Brand inactive");

        when(request.getRequestURI()).thenReturn("/brands/1");

        ResponseEntity<ErrorResponse> response = handler.handleBrandInactive(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("BRAND_INACTIVE", response.getBody().error());
        assertEquals("Brand inactive", response.getBody().message());
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequest_WhenValidationFails(){
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("brand", "name", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(request.getRequestURI()).thenReturn("/brands");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals("Validation failed", response.getBody().message());
        assertNotNull(response.getBody().errors());
        assertEquals(1, response.getBody().errors().size());
        assertEquals("name", response.getBody().errors().getFirst().field());
        assertEquals("must not be blank", response.getBody().errors().getFirst().message());
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnBadRequest_WhenParameterTypeIsInvalid(){
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);

        when(request.getRequestURI()).thenReturn("/brands/invalid");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatch(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals("Invalid parameter value", response.getBody().message());
    }

    @Test
    void handleObjectOptimisticLockingFailureException_ShouldReturnConflict_WhenConcurrentUpdateOccurs(){
        ObjectOptimisticLockingFailureException exception =
                 new ObjectOptimisticLockingFailureException(Brand.class, 1L);

        when(request.getRequestURI()).thenReturn("/brands/1");

        ResponseEntity<ErrorResponse> response =
                handler.handleObjectOptimisticLockingFailureException(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONCURRENT_UPDATE", response.getBody().error());
        assertEquals("This record has been updated by another user. Please reload the data and try again",
                response.getBody().message());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError_WhenUnexpectedExceptionOccurs(){
        RuntimeException exception = new RuntimeException("Unexpected error");

        when(request.getRequestURI()).thenReturn("/brands");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().error());
        assertEquals("An unexpected error occurred", response.getBody().message());
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