package com.github.senocak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.security.AuthorizationInterceptor;
import com.github.senocak.util.AppConstants;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@EnableAsync
@Configuration
public class AppConfig implements WebMvcConfigurer {
    private final AuthorizationInterceptor authorizationInterceptor;

    public AppConfig(AuthorizationInterceptor authorizationInterceptor) {
        this.authorizationInterceptor = authorizationInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index.html");
        registry.addRedirectViewController("/swagger", "/swagger-ui/index.html");
    }

    /**
     * Marking the files as resource
     * @param registry -- Stores registrations of resource handlers for serving static resources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("//**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * Add Spring MVC lifecycle interceptors for pre- and post-processing of controller method invocations
     * and resource handler requests.
     * @param registry -- List of mapped interceptors.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(authorizationInterceptor)
                .addPathPatterns("/api/v1/**");
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper){
        return new ModelResolver(objectMapper);
    }

    @Bean
    public OpenAPI customOpenAPI(
        @Value("${spring.application.name}") String title,
        @Value("${server.port}") String port,
        @Value("${springdoc.version}") String appVersion
    ) {
        SecurityScheme securitySchemesItem = new SecurityScheme()
                .name(AppConstants.TOKEN_PREFIX)
                .description("JWT auth description")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .in(SecurityScheme.In.HEADER)
                .bearerFormat("JWT");
        Info license = new Info().title(title).version(appVersion)
                .description(title)
                .termsOfService("https://github.com/senocak")
                .license(new License().name("Apache 2.0").url("https://springdoc.org"));
        Server server1 = new Server().url("http://localhost:"+port).description("Local Server");
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(AppConstants.TOKEN_PREFIX, securitySchemesItem))
                .info(license)
                .servers(List.of(server1));
    }

    @Bean
    public GroupedOpenApi accountApi() {
        return GroupedOpenApi.builder().displayName("User operations").group("user").pathsToMatch("/api/v1/user/**").build();
    }

    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder().displayName("Metric operations").group("actuator").pathsToMatch("/actuator/**").build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder().displayName("Auth operations").group("auth").pathsToMatch("/api/v1/auth/**").build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * We use the PasswordEncoder that is defined in the Spring Security configuration to encode the password.
     * @return -- singleton instance of PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
