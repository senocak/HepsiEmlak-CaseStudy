package com.github.senocak.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.config.SpringBootTestConfig;
import com.github.senocak.controller.AuthController;
import com.github.senocak.controller.BaseController;
import com.github.senocak.domain.User;
import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.exception.RestExceptionHandler;
import com.github.senocak.service.UserService;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.github.senocak.util.AppConstants.OmaErrorMessageType;
import com.github.senocak.util.AppConstants.RoleName;

import static com.github.senocak.TestConstants.USER_EMAIL;
import static com.github.senocak.TestConstants.USER_NAME;
import static com.github.senocak.TestConstants.USER_PASSWORD;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This integration test class is written for
 * @see AuthController
 */
@SpringBootTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration Tests for AuthController")
public class AuthControllerIT {
    @Autowired private AuthController authController;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RestExceptionHandler restExceptionHandler;
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(restExceptionHandler)
                .build();
    }

    @AfterAll
    public void afterAll() {
        userService.deleteAllUsers();
    }

    @BeforeAll
    public void beforeAll() {
        User user1 = new User("anil1", "anil1@senocak.com", passwordEncoder.encode("asenocak"),
                List.of(RoleName.ROLE_USER.getRole(), RoleName.ROLE_ADMIN.getRole()), new Date());
        user1.setId(UUID.fromString("2cb9374e-4e52-4142-a1af-16144ef4a27d"));
        userService.save(user1);

        User user2 = new User("anil2", "anil2@gmail.com", passwordEncoder.encode("asenocak"),
                List.of(RoleName.ROLE_USER.getRole()), new Date());
        user2.setId(UUID.fromString("3cb9374e-4e52-4142-a1af-16144ef4a27d"));
        userService.save(user2);

        User user3 = new User("anil3", "anil3@gmail.com", passwordEncoder.encode("asenocak"),
                List.of(RoleName.ROLE_USER.getRole()), null);
        user3.setId(UUID.fromString("4cb9374e-4e52-4142-a1af-16144ef4a27d"));
        userService.save(user3);
    }

    @Nested
    @Order(1)
    @DisplayName("Test class for login scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class LoginTest {
        private final LoginRequest request = new LoginRequest("", "");

        @Test
        @Order(1)
        @DisplayName("ServerException is expected since request body is not valid")
        void givenInvalidSchema_whenLogin_thenThrowServerException() throws Exception {
            // Given
            String requestBody = writeValueAsString(request);
            // When
            ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post(BaseController.V1_AUTH_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            // Then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                    .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())));
        }

        @Test
        @Order(2)
        @DisplayName("ServerException is expected since credentials are not valid")
        void givenInvalidCredentials_whenLogin_thenThrowServerException() throws Exception {
            // Given
            request.setEmail("anil1@senocak.com");
            request.setPassword("not_asenocak");
            String requestBody = writeValueAsString(request);
            // When
            ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post(BaseController.V1_AUTH_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            // Then
            perform.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.UNAUTHORIZED.value())))
                    .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.UNAUTHORIZED.getMessageId())))
                    .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.UNAUTHORIZED.getText())))
                    .andExpect(jsonPath("$.exception.variables", hasSize(1)))
                    .andExpect(jsonPath("$.exception.variables[0]", equalTo("Username or password invalid. AuthenticationCredentialsNotFoundException occurred for anil1")));
        }

        @Test
        @Order(3)
        @DisplayName("ServerException is expected since user not existed")
        void givenNotActivatedUser_whenLogin_thenThrowServerException() throws Exception {
            // Given
            request.setEmail("not@exist.com");
            request.setPassword("asenocak");
            String requestBody = writeValueAsString(request);
            // When
            ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post(BaseController.V1_AUTH_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            // Then
            perform.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.NOT_FOUND.value())))
                    .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.NOT_FOUND.getMessageId())))
                    .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.NOT_FOUND.getText())))
                    .andExpect(jsonPath("$.exception.variables", hasSize(1)))
                    .andExpect(jsonPath("$.exception.variables[0]", equalTo("user_not_found")));
        }

        @Test
        @Order(4)
        @DisplayName("Happy path")
        void given_whenLogin_thenReturn200() throws Exception {
            // Given
            request.setEmail("anil1@senocak.com");
            request.setPassword("asenocak");
            String requestBody = writeValueAsString(request);
            // When
            ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post(BaseController.V1_AUTH_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            // Then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.email", equalTo(request.getEmail())))
                    .andExpect(jsonPath("$.user.roles", hasSize(2)))
                    .andExpect(jsonPath("$.token", notNullValue()));
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Test class for register scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterTest {
        private final RegisterRequest registerRequest = new RegisterRequest("", "", "");

        @Test
        @Order(1)
        @DisplayName("ServerException is expected since request body is not valid")
        void givenInvalidSchema_whenRegister_thenThrowServerException() throws Exception {
            // Given
            String requestBody = writeValueAsString(registerRequest);
            // When
            ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post(BaseController.V1_AUTH_URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            // Then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                    .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())));
        }

        @Test
        @Order(2)
        @DisplayName("ServerException is expected since there is already user with username")
        void givenEmailExist_whenRegister_thenThrowServerException() throws Exception {
            // Given
            registerRequest.setName(USER_NAME);
            registerRequest.setEmail(USER_EMAIL);
            registerRequest.setPassword(USER_PASSWORD);
            String requestBody = writeValueAsString(registerRequest);
            // When
            ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post(BaseController.V1_AUTH_URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            // Then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getMessageId())))
                    .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.getText())))
                    .andExpect(jsonPath("$.exception.variables", hasSize(1)))
                    .andExpect(jsonPath("$.exception.variables[0]", equalTo("unique_email: " + USER_EMAIL)));
        }

        @Test
        @Order(3)
        @DisplayName("Happy path")
        void given_whenRegister_thenReturn201() throws Exception {
            // Given
            registerRequest.setName(USER_NAME);
            registerRequest.setEmail("userNew@email.com");
            registerRequest.setPassword(USER_PASSWORD);
            String requestBody = writeValueAsString(registerRequest);
            // When
            ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post(BaseController.V1_AUTH_URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            // Then
            perform.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message", notNullValue()))
                    .andExpect(jsonPath("$.message", equalTo("email_has_to_be_verified")));
        }
    }

    /**
     * @param value - an object that want to be serialized
     * @return - string
     * @throws JsonProcessingException - throws JsonProcessingException
     */
    private String writeValueAsString(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}