package com.github.senocak.controller;

import com.github.senocak.domain.TodoItem;
import com.github.senocak.dto.ExceptionDto;
import com.github.senocak.dto.todo.CreateTodoDto;
import com.github.senocak.dto.todo.TodoDto;
import com.github.senocak.dto.todo.TodoItemPaginationDTO;
import com.github.senocak.dto.todo.UpdateTodoDto;
import com.github.senocak.util.AppConstants.OmaErrorMessageType;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.dto.user.UserWrapperResponse;
import com.github.senocak.dto.user.UpdateUserDto;
import com.github.senocak.security.Authorize;
import com.github.senocak.service.DtoConverter;
import com.github.senocak.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.github.senocak.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.github.senocak.exception.ServerException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.UUID;
import static com.github.senocak.util.AppConstants.ADMIN;
import static com.github.senocak.util.AppConstants.DEFAULT_PAGE_NUMBER;
import static com.github.senocak.util.AppConstants.DEFAULT_PAGE_SIZE;
import static com.github.senocak.util.AppConstants.TOKEN_PREFIX;
import static com.github.senocak.util.AppConstants.USER;

@Validated
@RestController
@Authorize(roles = {ADMIN, USER})
@RequestMapping(BaseController.V1_USER_URL)
@Tag(name = "User", description = "User Controller")
public class UserController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get me",
        tags = {"User"},
        responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserWrapperResponse.class))),
            @ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
        },
        security = @SecurityRequirement(name = TOKEN_PREFIX, scopes = {ADMIN, USER})
    )
    public UserResponse me() {
        return DtoConverter.convertEntityToDto(userService.loggedInUser());
    }

    @PatchMapping("/me")
    @Operation(
        summary = "Update user by username",
        tags = {"User"},
        responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HashMap.class))),
            @ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
        },
        security = @SecurityRequirement(name = TOKEN_PREFIX, scopes = {ADMIN, USER})
    )
    public UserResponse patchMe(
        @Parameter(description = "Request body to update", required = true) @Validated @RequestBody UpdateUserDto userDto,
        BindingResult resultOfValidation
    ) throws ServerException {
        validate(resultOfValidation);
        User user = userService.loggedInUser();
        String name = userDto.getName();
        if (name != null && !name.isEmpty()) {
            user.setName(name);
        }
        String password = userDto.getPassword();
        String passwordConfirmation = userDto.getPassword_confirmation();
        if (password != null && !password.isEmpty()) {
            if (passwordConfirmation == null || passwordConfirmation.isEmpty()) {
                String errorMessage = "password_confirmation_not_provided";
                log.error(errorMessage);
                throw new ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, new String[]{errorMessage}, HttpStatus.BAD_REQUEST);
            }
            if (!password.equals(passwordConfirmation)) {
                String errorMessage = "password_and_confirmation_not_matched";
                log.error(errorMessage);
                throw new ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, new String[]{errorMessage}, HttpStatus.BAD_REQUEST);
            }
            user.setPassword(passwordEncoder.encode(password));
        }
        return DtoConverter.convertEntityToDto(userService.loggedInUser());
    }

    @GetMapping("/todos")
    @Operation(
        summary = "Get List of todos",
        tags = {"User"},
        responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserWrapperResponse.class))),
            @ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
        },
        security = @SecurityRequirement(name = TOKEN_PREFIX, scopes = {ADMIN, USER})
    )
    public TodoItemPaginationDTO todos(
        @Parameter(name = "page", description = "Page number", example = DEFAULT_PAGE_NUMBER) @RequestParam(defaultValue = "0", required = false) int page,
        @Parameter(name = "size", description = "Page size", example = DEFAULT_PAGE_SIZE) @RequestParam(defaultValue = "${spring.data.web.pageable.default-page-size:10}", required = false) int size
    ) {
        User owner = userService.loggedInUser();
        Page<TodoItem> byTodoItems = userService.findByTodoItems(owner.getId(), PageRequest.of(page, size));
        return new TodoItemPaginationDTO(byTodoItems,
                byTodoItems.getContent().stream().map(it -> DtoConverter.convertEntityToDto(it, owner)).toList(),
                null, null);
    }

    @PostMapping("/todos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create todo",
        tags = {"User"},
        responses = {
            @ApiResponse(responseCode = "201", description = "successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TodoDto.class))),
            @ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
        },
        security = @SecurityRequirement(name = TOKEN_PREFIX, scopes = {ADMIN, USER})
    )
    public TodoDto createTodo(
        @Parameter(description = "Request body", required = true) @Validated @RequestBody CreateTodoDto createTodo,
        BindingResult resultOfValidation
    ) throws ServerException {
        validate(resultOfValidation);
        User owner = userService.loggedInUser();
        return DtoConverter.convertEntityToDto(userService.createTodoItem(createTodo, owner), owner);
    }

    @GetMapping("/todos/{id}")
    @Operation(
        summary = "Get todo",
        tags = {"User"},
        responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TodoDto.class))),
            @ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
        },
        security = @SecurityRequirement(name = TOKEN_PREFIX, scopes = {ADMIN, USER})
    )
    public TodoDto getTodo(@Parameter(description = "Id", required = true) @PathVariable String id) throws ServerException {
        User owner = userService.loggedInUser();
        return DtoConverter.convertEntityToDto(userService.findTodoItem(UUID.fromString(id)), owner);
    }

    @PatchMapping("/todos/{id}")
    @Operation(
        summary = "Update todo",
        tags = {"User"},
        responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TodoDto.class))),
            @ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
        },
        security = @SecurityRequirement(name = TOKEN_PREFIX, scopes = {ADMIN, USER})
    )
    public TodoDto updateTodo(
        @Parameter(description = "Id", required = true) @PathVariable String id,
        @Parameter(description = "Request body", required = true) @Validated @RequestBody UpdateTodoDto updateTodoDto,
        BindingResult resultOfValidation
    ) throws ServerException {
        validate(resultOfValidation);
        User owner = userService.loggedInUser();
        return DtoConverter.convertEntityToDto(userService.updateTodoItem(UUID.fromString(id), updateTodoDto), owner);
    }

    @DeleteMapping("/todos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete todo",
        tags = {"User"},
        responses = {
            @ApiResponse(responseCode = "204", description = "successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionDto.class)))
        },
        security = @SecurityRequirement(name = TOKEN_PREFIX, scopes = {ADMIN, USER})
    )
    public void deleteTodo(@Parameter(description = "Id", required = true) @PathVariable String id) throws ServerException {
        userService.deleteTodoItem(UUID.fromString(id));
    }
}