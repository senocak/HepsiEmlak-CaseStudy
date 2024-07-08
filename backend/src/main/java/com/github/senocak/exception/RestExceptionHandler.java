package com.github.senocak.exception;

import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.github.senocak.dto.ExceptionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.github.senocak.util.AppConstants.OmaErrorMessageType;
import java.util.ArrayList;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler({
        AccessDeniedException.class,
        AuthenticationCredentialsNotFoundException.class,
        UnrecognizedPropertyException.class
    })
    public ResponseEntity<Object> handleUnAuthorized(Exception ex) {
        return generateResponseEntity(HttpStatus.UNAUTHORIZED, new String[]{ex.getMessage()},
                OmaErrorMessageType.UNAUTHORIZED);
    }

    @ExceptionHandler({
        NoHandlerFoundException.class,
        UsernameNotFoundException.class,
        NoResourceFoundException.class
    })
    public ResponseEntity<Object> handleNoHandlerFoundException(Exception ex) {
        return generateResponseEntity(HttpStatus.NOT_FOUND, new String[]{ex.getMessage()},
                OmaErrorMessageType.NOT_FOUND);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex) {
        ArrayList<String> errors = new ArrayList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            errors.add(((FieldError) error).getField() + ": " + error.getDefaultMessage());
        }
        return generateResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, errors.toArray(new String[0]),
                OmaErrorMessageType.GENERIC_SERVICE_ERROR);
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<Object> handleServerException(ServerException ex) {
        return generateResponseEntity(ex.getStatusCode(), ex.getVariables(), ex.getOmaErrorMessageType());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        return generateResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, new String[]{"server_error", ex.getMessage()},
                OmaErrorMessageType.GENERIC_SERVICE_ERROR);
    }

    private ResponseEntity<Object> generateResponseEntity(final HttpStatus httpStatus,
                                                          final String[] variables,
                                                          final OmaErrorMessageType omaErrorMessageType) {
        log.error("Exception is handled. HttpStatus: {}, OmaErrorMessageType: {}, variables: {}", httpStatus, omaErrorMessageType, variables);
        final ExceptionDto exceptionDto = new ExceptionDto();
        exceptionDto.setStatusCode(httpStatus.value());
        exceptionDto.setError(new ExceptionDto.OmaErrorMessageTypeDto(omaErrorMessageType.getMessageId(), omaErrorMessageType.getText()));
        exceptionDto.setVariables(variables);
        return ResponseEntity.status(httpStatus).body(exceptionDto);
    }
}

