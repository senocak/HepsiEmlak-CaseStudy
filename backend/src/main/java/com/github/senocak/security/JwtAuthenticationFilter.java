package com.github.senocak.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.exception.RestExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import com.github.senocak.service.UserService;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final AuthenticationManager authenticationManager;
    private final RestExceptionHandler restExceptionHandler;
    private final RequestMappingHandlerMapping requestHandlerMapping;

    private boolean running = false;
    private final Map<String, List<String>> protectedEndpoints = new HashMap<>();

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserService userService,
                                   ObjectMapper objectMapper,
                                   AuthenticationManager authenticationManager,
                                   RestExceptionHandler restExceptionHandler,
                                   @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestHandlerMapping) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.authenticationManager = authenticationManager;
        this.restExceptionHandler = restExceptionHandler;
        this.requestHandlerMapping = requestHandlerMapping;

        protectedEndpoints.put("GET", new ArrayList<>());
        protectedEndpoints.put("POST", new ArrayList<>());
        protectedEndpoints.put("PATCH", new ArrayList<>());
        protectedEndpoints.put("DELETE", new ArrayList<>());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            boolean isProtectedApiBeingInvoked = isProtectedRequest(request.getMethod(), request);
            if (isProtectedApiBeingInvoked) {
                String bearerToken = request.getHeader("Authorization");
                if (bearerToken == null) {
                    log.error("Bearer Token should be provided in Authorization header");
                    throw new AccessDeniedException("Bearer Token should be provided in Authorization header");
                }
                if (!bearerToken.startsWith("Bearer ")) {
                    log.error("Token should start with Bearer ");
                    throw new AccessDeniedException("Token should start with Bearer ");
                }
                String jwt = bearerToken.substring(7);
                String email = tokenProvider.getUserEmailFromJWT(jwt);
                UserDetails userDetails = userService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                authenticationManager.authenticate(authentication);
                log.trace("SecurityContext created");
            }
        } catch (Exception ex) {
            ResponseEntity<Object> responseEntity = restExceptionHandler.handleUnAuthorized(new RuntimeException(ex.getMessage()));
            response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            log.error("Could not set user authentication in security context. Error: {}", ExceptionUtils.getMessage(ex));
            return;
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Expose-Headers", "Content-Type, Access-Control-Expose-Headers, Authorization, X-Requested-With");
        filterChain.doFilter(request, response);
        log.trace("Filtering accessed for remote address: {}", request.getRemoteAddr());
    }

    @Override
    public void start() {
        init();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Initializes the class by gathering public endpoints for various HTTP methods.
     * It identifies designated public endpoints within the application's mappings
     * and adds them to separate lists based on their associated HTTP methods.
     * If OpenAPI is enabled, Swagger endpoints are also considered as public.
     */
    private void init() {
        requestHandlerMapping.getHandlerMethods().forEach((requestInfo, handlerMethod) -> {
            Set<RequestMethod> methods = requestInfo.getMethodsCondition().getMethods();
            if (!methods.isEmpty()) {
                for (RequestMethod method : methods) {
                    if (handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(Authorize.class) ||
                            handlerMethod.hasMethodAnnotation(Authorize.class)) {
                        protectedEndpoints.get(method.name()).addAll(requestInfo.getPathPatternsCondition().getPatternValues());
                    }
                }
            }
        });
    }

    /**
     * Checks if the provided HTTP request is directed towards an unsecured API endpoint.
     *
     * @param request The HTTP request to inspect.
     * @return `true` if the request is to an unsecured API endpoint, `false` otherwise.
     */
    private boolean isProtectedRequest(String httpMethod, HttpServletRequest request) {
        List<String> endpoints = protectedEndpoints.get(httpMethod);
        if (endpoints != null) {
            for (String apiPath : endpoints) {
                if (new AntPathMatcher().match(apiPath, request.getRequestURI())) {
                    return true;
                }
            }
        }
        return false;
    }
}