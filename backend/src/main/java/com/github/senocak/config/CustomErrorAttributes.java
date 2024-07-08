package com.github.senocak.config;

import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.RequestDispatcher;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!integration-test")
public class CustomErrorAttributes extends DefaultErrorAttributes {
    private static final Logger log = LoggerFactory.getLogger(CustomErrorAttributes.class);
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        Object errorMessage = webRequest.getAttribute(RequestDispatcher.ERROR_MESSAGE, RequestAttributes.SCOPE_REQUEST);
        ExceptionDto exceptionDto = new ExceptionDto();
        if (errorMessage != null) {
            AppConstants.OmaErrorMessageType omaErrorMessageType = AppConstants.OmaErrorMessageType.NOT_FOUND;
            exceptionDto.setStatusCode(Integer.parseInt(errorAttributes.get("status").toString()));
            exceptionDto.setVariables(new String[]{errorAttributes.get("error").toString(), errorAttributes.get("message").toString()});
            exceptionDto.setError(new ExceptionDto.OmaErrorMessageTypeDto(omaErrorMessageType.getMessageId(), omaErrorMessageType.getText()));
        }
        HashMap<String, Object> response = new HashMap<>();
        response.put("response", exceptionDto);
        log.warn("Exception occurred in DefaultErrorAttributes: {}", response);
        return response;
    }
}
