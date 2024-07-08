package com.github.senocak.exception;

import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.util.AppConstants.OmaErrorMessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@DisplayName("Unit Tests for RestExceptionHandler")
class RestExceptionHandlerTest {
    private final RestExceptionHandler restExceptionHandler = new RestExceptionHandler();

    @Test
    void givenException_whenHandleUnAuthorized_thenAssertResult() {
        // Given
        RuntimeException ex = new AccessDeniedException("lorem");

        // When
        ResponseEntity<Object> handleResponse = restExceptionHandler.handleUnAuthorized(ex);

        // Then
        assertNotNull(handleResponse.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, handleResponse.getStatusCode());

        ExceptionDto exceptionDto = (ExceptionDto) handleResponse.getBody();
        assertNotNull(exceptionDto);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), exceptionDto.getStatusCode());
        assertEquals(OmaErrorMessageType.UNAUTHORIZED.getMessageId(), exceptionDto.getError().getId());
        assertEquals(OmaErrorMessageType.UNAUTHORIZED.getText(), exceptionDto.getError().getText());
        assertEquals(1, exceptionDto.getVariables().length);
        assertEquals(ex.getMessage(), exceptionDto.getVariables()[0]);
    }

    @Test
    void givenException_whenHandleNoHandlerFoundException_thenAssertResult() {
        // Given
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "", new HttpHeaders());

        // When
        ResponseEntity<Object> handleResponse = restExceptionHandler.handleNoHandlerFoundException(ex);

        // Then
        assertNotNull(handleResponse.getBody());
        assertEquals(HttpStatus.NOT_FOUND, handleResponse.getStatusCode());

        ExceptionDto exceptionDto = (ExceptionDto) handleResponse.getBody();
        assertNotNull(exceptionDto);
        assertEquals(HttpStatus.NOT_FOUND.value(), exceptionDto.getStatusCode());
        assertEquals(OmaErrorMessageType.NOT_FOUND.getMessageId(), exceptionDto.getError().getId());
        assertEquals(OmaErrorMessageType.NOT_FOUND.getText(), exceptionDto.getError().getText());
        assertEquals(1, exceptionDto.getVariables().length);
        assertEquals("No endpoint GET .", exceptionDto.getVariables()[0]);
    }

    @Test
    void givenException_whenHandleBindException_thenAssertResult() {
        // Given
        BindException bindException = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindException.getBindingResult()).thenReturn(bindingResult);

        FieldError fieldError = new FieldError("name", "message", "default");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<Object> handleResponse = restExceptionHandler.handleBindException(bindException);

        // Then
        assertNotNull(handleResponse.getBody());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, handleResponse.getStatusCode());

        ExceptionDto exceptionDto = (ExceptionDto) handleResponse.getBody();
        assertNotNull(exceptionDto);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exceptionDto.getStatusCode());
        assertEquals(OmaErrorMessageType.GENERIC_SERVICE_ERROR.getMessageId(), exceptionDto.getError().getId());
        assertEquals(OmaErrorMessageType.GENERIC_SERVICE_ERROR.getText(), exceptionDto.getError().getText());
        assertEquals(1, exceptionDto.getVariables().length);
        assertEquals("message: default", exceptionDto.getVariables()[0]);
    }

    @Test
    void givenException_whenHandleServerException_thenAssertResult() {
        // Given
        String errMsg = "lorem";
        ServerException ex = new ServerException(OmaErrorMessageType.NOT_FOUND, new String[]{errMsg}, HttpStatus.CONFLICT);

        // When
        ResponseEntity<Object> handleResponse = restExceptionHandler.handleServerException(ex);

        // Then
        assertNotNull(handleResponse.getBody());
        assertEquals(HttpStatus.CONFLICT, handleResponse.getStatusCode());

        ExceptionDto exceptionDto = (ExceptionDto) handleResponse.getBody();
        assertNotNull(exceptionDto);
        assertEquals(HttpStatus.CONFLICT.value(), exceptionDto.getStatusCode());
        assertEquals(OmaErrorMessageType.NOT_FOUND.getMessageId(), exceptionDto.getError().getId());
        assertEquals(OmaErrorMessageType.NOT_FOUND.getText(), exceptionDto.getError().getText());
        assertEquals(1, exceptionDto.getVariables().length);
        assertEquals(errMsg, exceptionDto.getVariables()[0]);
    }

    @Test
    void givenException_whenHandleGeneralException_thenAssertResult() {
        // Given
        Exception ex = new Exception("lorem");

        // When
        ResponseEntity<Object> handleResponse = restExceptionHandler.handleGeneralException(ex);

        // Then
        assertNotNull(handleResponse.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handleResponse.getStatusCode());

        ExceptionDto exceptionDto = (ExceptionDto) handleResponse.getBody();
        assertNotNull(exceptionDto);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exceptionDto.getStatusCode());
        assertEquals(OmaErrorMessageType.GENERIC_SERVICE_ERROR.getMessageId(), exceptionDto.getError().getId());
        assertEquals(OmaErrorMessageType.GENERIC_SERVICE_ERROR.getText(), exceptionDto.getError().getText());
        assertEquals(2, exceptionDto.getVariables().length);
        assertEquals("server_error", exceptionDto.getVariables()[0]);
        assertEquals(ex.getMessage(), exceptionDto.getVariables()[1]);
    }
}
