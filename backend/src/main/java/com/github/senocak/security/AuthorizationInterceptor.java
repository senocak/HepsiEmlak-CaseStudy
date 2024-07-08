package com.github.senocak.security;

import com.github.senocak.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AuthorizationInterceptor implements AsyncHandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);
    private final AuthenticationService authenticationService;

    public AuthorizationInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Interception point before the execution of a handler.
     * @param request -- Request information for HTTP servlets.
     * @param response -- It is where the servlet can write information about the data it will send back.
     * @param handler -- Class Object is the root of the class hierarchy.
     * @return -- true or false or AccessDeniedException
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        validateQueryParams(request, handlerMethod);
        Authorize authorizeAnnotation = getAuthorizeAnnotation(handlerMethod);
        if (authorizeAnnotation != null && !hasAnnotationRole(authorizeAnnotation)) {
            log.error("Throwing AccessDeniedException because role is not valid for api");
            throw new AccessDeniedException("You are not allowed to perform this operation");
        }
        return true;
    }

    /**
     * Validation of the request params to check unhandled ones
     * @param request -- Request information for HTTP servlets.
     * @param handler -- Encapsulates information about a handler method consisting of a method
     */
    private void validateQueryParams(HttpServletRequest request, HandlerMethod handler) {
        List<String> queryParams = Collections.list(request.getParameterNames());
        HandlerMethodArgumentResolverComposite resolverComposite = new HandlerMethodArgumentResolverComposite();
        List<String> expectedParams = new ArrayList<>();

        for (MethodParameter methodParameter : handler.getMethodParameters()) {
            RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
            if (requestParam != null) {
                if (!requestParam.name().isEmpty()) {
                    expectedParams.add(requestParam.name());
                } else {
                    methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
                    expectedParams.add(methodParameter.getParameterName());
                }
            }
        }

        queryParams.removeAll(expectedParams);
        if (!queryParams.isEmpty()) {
            log.error("Unexpected parameters: {}", queryParams);
            throw new InvalidParameterException("unexpected parameter: " + queryParams);
        }
    }

    /**
     * Get infos for Authorize annotation that defined for class or method
     * @param handlerMethod -- RequestMapping method that reached to server
     * @return -- Authorize annotation or null
     */
    private Authorize getAuthorizeAnnotation(HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().isAnnotationPresent(Authorize.class)) {
            return handlerMethod.getMethod().getAnnotation(Authorize.class);
        }
        if (handlerMethod.getBeanType().isAnnotationPresent(Authorize.class)) {
            return handlerMethod.getBeanType().getAnnotation(Authorize.class);
        }
        return null;
    }

    /**
     * Checks the roles of user for defined Authorize annotation
     * @param authorize - parameter that has roles
     * @return -- false if not authorized
     * @throws BadCredentialsException -- throws BadCredentialsException
     * @throws AccessDeniedException -- throws AccessDeniedException
     */
    private boolean hasAnnotationRole(Authorize authorize) {
        try {
            return authenticationService.isAuthorized(authorize.roles());
        } catch (Exception ex) {
            log.trace("Exception occurred while authorizing. Exception: {}", ex.getMessage());
            return false;
        }
    }
}