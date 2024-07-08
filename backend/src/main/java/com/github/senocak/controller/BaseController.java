package com.github.senocak.controller;

import com.github.senocak.exception.ServerException;
import com.github.senocak.util.AppConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
public abstract class BaseController {

    protected void validate(BindingResult resultOfValidation) throws ServerException {
        if (resultOfValidation.hasErrors()) {
            final List<String> errors = new ArrayList<>();
            for (FieldError fieldError : resultOfValidation.getFieldErrors()) {
                errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
            }
            for (ObjectError objectError : resultOfValidation.getGlobalErrors()) {
                errors.add(objectError.getDefaultMessage());
            }
            throw new ServerException(AppConstants.OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                    new String[0], HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creates an HTTP header containing a user ID.
     * @param userId The user ID to include in the header.
     * @return The HttpHeaders object containing the user ID header.
     */
    protected HttpHeaders userIdHeader(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("userId", userId);
        return headers;
    }

    public static final String API = "/api";
    public static final String V1 = API + "/v1";
    public static final String V1_AUTH_URL = V1 + "/auth";
    public static final String V1_USER_URL = V1 + "/user";
}