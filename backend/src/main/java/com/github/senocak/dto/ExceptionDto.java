package com.github.senocak.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonPropertyOrder({"statusCode", "error", "variables"})
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName("exception")
public class ExceptionDto extends BaseDto {
    private int statusCode;
    private OmaErrorMessageTypeDto error;
    private String[] variables;

    @JsonPropertyOrder({"id", "text"})
    public static class OmaErrorMessageTypeDto{
        private String id;
        private String text;

        public OmaErrorMessageTypeDto(String id, String text) {
            this.id = id;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public OmaErrorMessageTypeDto getError() {
        return error;
    }

    public void setError(OmaErrorMessageTypeDto error) {
        this.error = error;
    }

    public String[] getVariables() {
        return variables;
    }

    public void setVariables(String[] variables) {
        this.variables = variables;
    }
}

