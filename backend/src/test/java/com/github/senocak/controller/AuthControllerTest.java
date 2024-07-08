package com.github.senocak.controller;

import com.github.senocak.domain.User;
import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.security.JwtTokenProvider;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants.OmaErrorMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for AuthController")
class AuthControllerTest {
    private AuthController authController;
    @Mock private UserService userService;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Authentication authentication;
    @Mock private BindingResult bindingResult;

    private User user;

    @BeforeEach
    void init() {
        authController = new AuthController(
                userService,
                tokenProvider,
                passwordEncoder,
                authenticationManager,
                100L
        );
        this.user = new User("Lucienne", "anil1@senocak.com", "stanford.Pollich14",
                List.of("ROLE_USER", "ROLE_ADMIN"), new Date());
        this.user.setId(UUID.randomUUID());
    }

    @Nested
    class LoginTest {
        private LoginRequest loginRequest;

        @BeforeEach
        void setup() {
            loginRequest = new LoginRequest();
            loginRequest.setEmail("anil1@senocak.com");
            loginRequest.setPassword("stanford.Pollich14");
        }

        @Test
        void givenNotActivatedUser_whenLogin_thenThrowException() {
            // Given
            user.setEmailActivatedAt(null);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);
            // When
            Executable response = () -> authController.login(loginRequest, bindingResult);
            // Then
            ServerException exception = assertThrows(ServerException.class, response);
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            assertEquals(OmaErrorMessageType.UNAUTHORIZED, exception.getOmaErrorMessageType());
            assertEquals(1, exception.getVariables().length);
            assertEquals("email_not_activated", exception.getVariables()[0]);
        }

        @Test
        void givenSuccessfulPath_whenLogin_thenReturn200() throws ServerException {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);
            String generatedToken = "generatedToken";
            when(tokenProvider.generateJwtToken(eq(user.getEmail()), anyList())).thenReturn(generatedToken);

            // When
            ResponseEntity<UserWrapperResponse> response = authController.login(loginRequest, bindingResult);

            // Then
            assertNotNull(response);
            assertNotNull(response.getBody());
            assertEquals(generatedToken, response.getBody().getToken());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(user.getName(), response.getBody().getUserResponse().getName());
            assertEquals(user.getEmail(), response.getBody().getUserResponse().getEmail());
            assertEquals(user.getRoles().size(), response.getBody().getUserResponse().getRoles().size());
            assertEquals("generatedToken", response.getBody().getToken());
        }
    }

    @Nested
    class RegisterTest {
        private RegisterRequest registerRequest;

        @BeforeEach
        void setup() {
            registerRequest = new RegisterRequest();
            registerRequest.setName("Lucienne");
            registerRequest.setEmail("anil1@senocak.com");
            registerRequest.setPassword("stanford.Pollich14");
        }

        @Test
        void givenExistMail_whenRegister_thenThrowServerException() {
            // Given
            when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            // When
            Executable closureToTest = () -> authController.register(registerRequest, bindingResult);

            // Then
            assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenRegister_thenAssertResult() throws ServerException {
            // Given
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("pass1");
            when(userService.save(any(User.class))).thenReturn(user);

            // When
            Map<String, String> response = authController.register(registerRequest, bindingResult);

            // Then
            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("email_has_to_be_verified", response.get("message"));
        }
    }
}