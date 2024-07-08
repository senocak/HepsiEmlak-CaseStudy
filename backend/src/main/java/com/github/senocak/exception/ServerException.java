package com.github.senocak.exception;

import com.github.senocak.util.AppConstants;
import org.springframework.http.HttpStatus;

public class ServerException extends Exception {
    private final AppConstants.OmaErrorMessageType omaErrorMessageType;
    private final String[] variables;
    private final HttpStatus statusCode;

    public ServerException(AppConstants.OmaErrorMessageType omaErrorMessageType, String[] variables, HttpStatus statusCode) {
        this.omaErrorMessageType = omaErrorMessageType;
        this.variables = variables;
        this.statusCode = statusCode;
    }

    public AppConstants.OmaErrorMessageType getOmaErrorMessageType() {
        return omaErrorMessageType;
    }

    public String[] getVariables() {
        return variables;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
