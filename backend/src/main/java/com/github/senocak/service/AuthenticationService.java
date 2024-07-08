package com.github.senocak.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;

public interface AuthenticationService {
    String AUTHORIZATION_FAILED = "Authentication error";

    boolean isAuthorized(String... aInRoles) throws AccessDeniedException;
    User getPrinciple();
}
