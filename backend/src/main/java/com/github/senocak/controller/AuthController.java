package com.github.senocak.controller;

import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.auth.LoginRequest;
import com.github.senocak.dto.auth.RegisterRequest;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.util.AppConstants.OmaErrorMessageType;
import com.github.senocak.util.AppConstants.RoleName;
import com.github.senocak.domain.User;
import com.github.senocak.security.JwtTokenProvider;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(BaseController.V1_AUTH_URL)
@Tag(name = "Authentication", description = "AUTH API")
@CrossOrigin
public class AuthController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final long jwtExpirationInMs;

    public AuthController(
            UserService userService,
            JwtTokenProvider tokenProvider,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            @Value("${app.jwtExpirationInMs}") long jwtExpirationInMs) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    @PostMapping("/login")
    @Operation(summary = "Login Endpoint", tags = {"Authentication"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserWrapperResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    })
    public ResponseEntity<UserWrapperResponse> login(
        @Parameter(description = "Request body to login", required = true) @Validated @RequestBody LoginRequest loginRequest,
        BindingResult resultOfValidation
    ) throws ServerException {
        validate(resultOfValidation);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        User user = userService.findByEmail(loginRequest.getEmail());
        if (user.getEmailActivatedAt() == null) {
            String msg = "email_not_activated";
            log.error(msg);
            throw new ServerException(OmaErrorMessageType.UNAUTHORIZED, new String[]{msg}, HttpStatus.UNAUTHORIZED);
        }
        UserWrapperResponse userWrapperResponse = generateUserWrapperResponse(user);
        HttpHeaders httpHeaders = userIdHeader(String.valueOf(user.getId()));
        httpHeaders.add("jwtExpiresIn", String.valueOf(jwtExpirationInMs));
        return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).body(userWrapperResponse);
    }

    @PostMapping("/register")
    @Operation(summary = "Register Endpoint", tags = {"Authentication"}, responses = {
        @ApiResponse(responseCode = "200", description = "successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserWrapperResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class))),
        @ApiResponse(responseCode = "500", description = "internal server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
    })
    @ResponseStatus(code = HttpStatus.CREATED)
    public Map<String, String> register(
        @Parameter(description = "Request body to register", required = true) @Validated @RequestBody RegisterRequest signUpRequest,
        BindingResult resultOfValidation
    ) throws ServerException {
        validate(resultOfValidation);
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            String errorMessage = "unique_email: " + signUpRequest.getEmail();
            log.error(errorMessage);
            throw new ServerException(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, new String[]{errorMessage}, HttpStatus.BAD_REQUEST);
        }
        User user = new User(signUpRequest.getName(), signUpRequest.getEmail(), passwordEncoder.encode(signUpRequest.getPassword()),
                List.of(RoleName.ROLE_USER.getRole()), null);
        user.setId(UUID.randomUUID());
        User savedUser = userService.save(user);
        log.info("User created. User: {}", savedUser);
        return Map.of("message", "email_has_to_be_verified");
    }

    /**
     * Generate UserWrapperResponse with given UserResponse
     *
     * @param user User entity that contains user data
     * @return UserWrapperResponse
     */
    private UserWrapperResponse generateUserWrapperResponse(User user) {
        UserResponse userResponse = DtoConverter.convertEntityToDto(user);
        String jwtToken = tokenProvider.generateJwtToken(user.getEmail(), userResponse.getRoles());
        UserWrapperResponse userWrapperResponse = new UserWrapperResponse(userResponse, jwtToken);
        log.info("UserWrapperResponse is generated. UserWrapperResponse: {}", userWrapperResponse);
        return userWrapperResponse;
    }
}