package com.github.senocak.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.exception.RestExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;
    private final RestExceptionHandler restExceptionHandler;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper, RestExceptionHandler restExceptionHandler) {
        this.objectMapper = objectMapper;
        this.restExceptionHandler = restExceptionHandler;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        log.error("Responding with unauthorized error. Message - {}", ex.getMessage());
        ResponseEntity<Object> responseEntity = restExceptionHandler.handleUnAuthorized(new RuntimeException(ex.getMessage()));
        response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
