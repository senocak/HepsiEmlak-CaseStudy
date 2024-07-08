package com.github.senocak.util;

public class AppConstants {
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String TOKEN_HEADER_NAME = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    public enum RoleName {
        ROLE_USER(USER),
        ROLE_ADMIN(ADMIN);
        private final String role;

        RoleName(String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }

        public static RoleName fromString(String r) {
            for (RoleName roleName : RoleName.values()) {
                if (roleName.role.equals(r) || roleName.name().equals(r)) {
                    return roleName;
                }
            }
            return null;
        }
    }

    public enum OmaErrorMessageType {
        BASIC_INVALID_INPUT("SVC0001", "Invalid input value for message part %1"),
        GENERIC_SERVICE_ERROR("SVC0002", "The following service error occurred: %1. Error code is %2"),
        DETAILED_INVALID_INPUT("SVC0003", "Invalid input value for %1 %2: %3"),
        EXTRA_INPUT_NOT_ALLOWED("SVC0004", "Input %1 %2 not permitted in request"),
        MANDATORY_INPUT_MISSING("SVC0005", "Mandatory input %1 %2 is missing from request"),
        UNAUTHORIZED("SVC0006", "UnAuthorized Endpoint"),
        JSON_SCHEMA_VALIDATOR("SVC0007", "Schema failed."),
        NOT_FOUND("SVC0008", "Entry is not found");

        private final String messageId;
        private final String text;

        OmaErrorMessageType(String messageId, String text) {
            this.messageId = messageId;
            this.text = text;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getText() {
            return text;
        }
    }
}
