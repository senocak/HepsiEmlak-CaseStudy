package com.github.senocak.service;

import com.github.senocak.domain.TodoItem;
import com.github.senocak.domain.User;
import com.github.senocak.dto.todo.CreateTodoDto;
import com.github.senocak.dto.todo.UpdateTodoDto;
import com.github.senocak.exception.ServerException;
import com.github.senocak.repository.TodoItemRepository;
import com.github.senocak.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.senocak.TestDataFactory.createTestTodo;
import static com.github.senocak.TestDataFactory.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserService")
class UserServiceTest {
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final TodoItemRepository todoItemRepository = Mockito.mock(TodoItemRepository.class);
    private final Authentication auth = Mockito.mock(Authentication.class);
    private final org.springframework.security.core.userdetails.User securityUser = Mockito.mock(org.springframework.security.core.userdetails.User.class);

    @InjectMocks private UserService userService;

    private final User user = createTestUser();
    private final TodoItem todo = createTestTodo();

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, todoItemRepository);
    }

    @Test
    void givenUsername_whenExistsByEmail_thenAssertResult() {
        // When
        boolean existsByEmail = userService.existsByEmail("username");
        // Then
        assertFalse(existsByEmail);
    }

    @Test
    void givenEmail_whenFindByEmail_thenAssertResult() {
        // Given
        User user = createTestUser();
        when(userRepository.findByEmail("email")).thenReturn(user);
        // When
        User findByUsername = userService.findByEmail("email");
        // Then
        assertEquals(user, findByUsername);
    }

    @Test
    void givenNullEmail_whenFindByEmail_thenAssertResult() {
        // When
        Executable closureToTest = () -> userService.findByEmail("email");
        // Then
        assertThrows(UsernameNotFoundException.class, closureToTest);
    }

    @Test
    void givenUser_whenSave_thenAssertResult() {
        // Given
        User user = createTestUser();
        when(userRepository.save(user)).thenReturn(user);
        // When
        User save = userService.save(user);
        // Then
        assertEquals(user, save);
    }

    @Test
    void givenNullUsername_whenLoadUserByUsername_thenAssertResult() {
        // When
        Executable closureToTest = () -> userService.loadUserByUsername("username");
        // Then
        assertThrows(UsernameNotFoundException.class, closureToTest);
    }

    @Test
    void givenUsername_whenLoadUserByUsername_thenAssertResult() {
        // Given
        User user = createTestUser();
        when(userRepository.findByEmail("email")).thenReturn(user);
        // When
        UserDetails loadUserByUsername = userService.loadUserByUsername("email");
        // Then
        assertEquals(user.getEmail(), loadUserByUsername.getUsername());
    }

    @Test
    void givenNotLoggedIn_whenLoadUserByUsername_thenAssertResult() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getPrincipal()).thenReturn(securityUser);
        when(securityUser.getUsername()).thenReturn("user");
        // When
        Executable closureToTest = () -> userService.loggedInUser();
        // Then
        assertThrows(UsernameNotFoundException.class, closureToTest);
    }

    @Test
    void givenLoggedIn_whenLoadUserByUsername_thenAssertResult() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getPrincipal()).thenReturn(securityUser);
        when(securityUser.getUsername()).thenReturn("email");
        User user = createTestUser();
        when(userRepository.findByEmail("email")).thenReturn(user);
        // When
        User loggedInUser = userService.loggedInUser();
        // Then
        assertEquals(user.getEmail(), loggedInUser.getEmail());
    }

    @Test
    void givenIdAndPageable_whenFindByTodoItems_thenAssertResult() {
        // Given
        UUID id = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        PageImpl<TodoItem> pages = new PageImpl<>(List.of(todo));
        when(todoItemRepository.findAllByOwner(id, pageable)).thenReturn(pages);
        // When
        Page<TodoItem> response = userService.findByTodoItems(id, pageable);
        // Then
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals(todo.getId(), response.getContent().get(0).getId());
        assertEquals(todo.getDescription(), response.getContent().get(0).getDescription());
        assertEquals(todo.getFinished(), response.getContent().get(0).getFinished());
    }

    @Test
    void givenId_whenFindTodoItem_thenAssertResult() throws ServerException {
        // Given
        UUID id = UUID.randomUUID();
        when(todoItemRepository.findById(id)).thenReturn(Optional.of(todo));
        // When
        TodoItem response = userService.findTodoItem(id);
        // Then
        assertEquals(todo.getId(), response.getId());
        assertEquals(todo.getDescription(), response.getDescription());
        assertEquals(todo.getFinished(), response.getFinished());
    }

    @Test
    void givenId_whenFindTodoItem_thenThrowServerException() {
        // Given
        UUID id = UUID.randomUUID();
        when(todoItemRepository.findById(id)).thenReturn(Optional.empty());
        // When
        Executable response = () -> userService.findTodoItem(id);
        // Then
        assertThrows(ServerException.class, response);
    }

    @Test
    void givenCreateTodoItem_thenAssertResult() {
        // Given
        CreateTodoDto createTodo = new CreateTodoDto("description");
        when(todoItemRepository.save(any(TodoItem.class))).thenReturn(todo);
        // When
        TodoItem response = userService.createTodoItem(createTodo, user);
        // Then
        assertEquals(todo.getId(), response.getId());
        assertEquals(createTodo.getDescription(), response.getDescription());
        assertFalse(response.getFinished());
    }

    @Test
    void givenIdAndUpdateTodoItem_thenAssertResult() throws ServerException {
        // Given
        UUID id = UUID.randomUUID();
        when(todoItemRepository.findById(id)).thenReturn(Optional.of(todo));
        UpdateTodoDto updateTodoDto = new UpdateTodoDto("description", true);
        when(todoItemRepository.save(any(TodoItem.class))).thenReturn(todo);
        // When
        TodoItem response = userService.updateTodoItem(id, updateTodoDto);
        // Then
        assertEquals(todo.getId(), response.getId());
        assertEquals(updateTodoDto.getDescription(), response.getDescription());
        assertEquals(updateTodoDto.getFinished(), response.getFinished());
    }

    @Test
    void givenId_whenDeleteTodoItem_thenAssertResult() throws ServerException {
        // Given
        UUID id = UUID.randomUUID();
        when(todoItemRepository.findById(id)).thenReturn(Optional.of(todo));
        // When
        userService.deleteTodoItem(id);
        // Then
        verify(todoItemRepository).delete(todo);
    }
}