package com.github.senocak.controller;

import com.github.senocak.domain.TodoItem;
import com.github.senocak.domain.User;
import com.github.senocak.dto.todo.CreateTodoDto;
import com.github.senocak.dto.todo.TodoDto;
import com.github.senocak.dto.todo.TodoItemPaginationDTO;
import com.github.senocak.dto.todo.UpdateTodoDto;
import com.github.senocak.dto.user.UpdateUserDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.exception.ServerException;
import com.github.senocak.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserController")
class UserControllerTest {
    @InjectMocks private UserController userController;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private BindingResult bindingResult;

    private User user;
    private TodoItem todo;

    @BeforeEach
    void init() {
        user = new User("Lucienne", "anil1@senocak.com", "stanford.Pollich14",
                List.of("ROLE_USER", "ROLE_ADMIN"), new Date());
        user.setId(UUID.randomUUID());

        todo = new TodoItem("description", UUID.randomUUID(), false);
        todo.setId(UUID.randomUUID());
    }

    @Nested
    class GetMeTest {
        @Test
        void given_whenGetMe_thenReturn200() {
            // Given
            when(userService.loggedInUser()).thenReturn(user);

            // When
            UserResponse getMe = userController.me();

            // Then
            assertNotNull(getMe);
            assertEquals(user.getEmail(), getMe.getEmail());
            assertEquals(user.getName(), getMe.getName());
        }
    }

    @Nested
    class PatchMeTest {
        private UpdateUserDto updateUserDto;

        @BeforeEach
        void setup() {
            updateUserDto = new UpdateUserDto();
        }

        @Test
        void givenNullPasswordConf_whenPatchMe_thenThrowServerException() {
            // Given
            when(userService.loggedInUser()).thenReturn(user);
            updateUserDto.setPassword("pass1");

            // When
            Executable closureToTest = () -> userController.patchMe(updateUserDto, bindingResult);

            // Then
            assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void givenInvalidPassword_whenPatchMe_thenThrowServerException() {
            // Given
            when(userService.loggedInUser()).thenReturn(user);
            updateUserDto.setPassword("pass1");
            updateUserDto.setPassword_confirmation("pass2");

            // When
            Executable closureToTest = () -> userController.patchMe(updateUserDto, bindingResult);

            // Then
            assertThrows(ServerException.class, closureToTest);
        }

        @Test
        void given_whenPatchMe_thenReturn200() throws ServerException {
            // Given
            when(userService.loggedInUser()).thenReturn(user);
            updateUserDto.setName("Lucienne");
            updateUserDto.setPassword("pass1");
            updateUserDto.setPassword_confirmation("pass1");
            when(passwordEncoder.encode("pass1")).thenReturn("pass1");

            // When
            UserResponse patchMe = userController.patchMe(updateUserDto, bindingResult);

            // Then
            assertNotNull(patchMe);
            assertEquals(user.getEmail(), patchMe.getEmail());
            assertEquals(user.getName(), patchMe.getName());
        }
    }

    @Nested
    class TodosTest {

        @Test
        void given_whenGetTodos_thenReturn200() {
            // Given
            when(userService.loggedInUser()).thenReturn(user);
            PageImpl<TodoItem> pages = new PageImpl<>(List.of(todo));
            when(userService.findByTodoItems(eq(user.getId()), any(PageRequest.class))).thenReturn(pages);

            // When
            TodoItemPaginationDTO response = userController.todos(0, 100);

            // Then
            assertEquals(1, response.getPages());
            assertEquals(1, response.getPage());
            assertEquals(1, response.getTotal());
            assertEquals(1, response.getItems().size());
            assertEquals(todo.getId().toString(), response.getItems().get(0).getId());
            assertEquals(todo.getDescription(), response.getItems().get(0).getDescription());
            assertEquals(todo.getFinished(), response.getItems().get(0).getFinished());
        }

        @Test
        void given_whenCreateTodo_thenReturn201() throws ServerException {
            // Given
            CreateTodoDto createTodo = new CreateTodoDto("description");

            when(userService.loggedInUser()).thenReturn(user);
            when(userService.createTodoItem(eq(createTodo), eq(user))).thenReturn(todo);

            // When
            TodoDto response = userController.createTodo(createTodo, bindingResult);

            // Then
            assertEquals(todo.getId().toString(), response.getId());
            assertEquals(createTodo.getDescription(), response.getDescription());
            assertFalse(response.getFinished());
        }

        @Test
        void given_whenGetTodo_thenReturn200() throws ServerException {
            // Given
            UUID id = UUID.randomUUID();
            when(userService.loggedInUser()).thenReturn(user);
            when(userService.findTodoItem(id)).thenReturn(todo);

            // When
            TodoDto response = userController.getTodo(id.toString());

            // Then
            assertEquals(todo.getId().toString(), response.getId());
            assertEquals(todo.getDescription(), response.getDescription());
            assertFalse(response.getFinished());
        }

        @Test
        void given_whenUpdateTodo_thenReturn200() throws ServerException {
            // Given
            UUID id = UUID.randomUUID();
            UpdateTodoDto updateTodoDto = new UpdateTodoDto("description", true);

            when(userService.loggedInUser()).thenReturn(user);
            when(userService.updateTodoItem(eq(id), eq(updateTodoDto))).thenReturn(todo);

            // When
            TodoDto response = userController.updateTodo(id.toString(), updateTodoDto, bindingResult);

            // Then
            assertEquals(todo.getId().toString(), response.getId());
            assertEquals(todo.getDescription(), response.getDescription());
            assertFalse(response.getFinished());
        }

        @Test
        void given_whenDeleteTodo_thenReturn200() throws ServerException {
            // Given
            UUID id = UUID.randomUUID();
            // When
            userController.deleteTodo(id.toString());

            // Then
            verify(userService).deleteTodoItem(id);
        }
    }
}