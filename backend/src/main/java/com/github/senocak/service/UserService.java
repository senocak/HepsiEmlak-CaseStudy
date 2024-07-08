package com.github.senocak.service;

import com.github.senocak.domain.TodoItem;
import com.github.senocak.domain.User;
import com.github.senocak.dto.todo.CreateTodoDto;
import com.github.senocak.dto.todo.UpdateTodoDto;
import com.github.senocak.repository.TodoItemRepository;
import com.github.senocak.util.AppConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.github.senocak.repository.UserRepository;
import com.github.senocak.exception.ServerException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TodoItemRepository todoItemRepository;

    public UserService(UserRepository userRepository, TodoItemRepository todoItemRepository) {
        this.userRepository = userRepository;
        this.todoItemRepository = todoItemRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * @param email -- string email to find in db
     * @return -- true or false
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * @param email -- string email to find in db
     * @return -- User object
     * @throws UsernameNotFoundException -- throws UsernameNotFoundException
     */
    public User findByEmail(String email) {
        User byEmail = userRepository.findByEmail(email);
        if (byEmail == null)
            throw new UsernameNotFoundException("user_not_found");
        return byEmail;
    }

    /**
     * @param user -- User object to persist to db
     * @return -- User object that is persisted to db
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    /**
     * @param email -- id
     * @return -- Spring User object
     */
    @Transactional
    @Override
    public org.springframework.security.core.userdetails.User loadUserByUsername(String email) {
        User user = findByEmail(email);
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(AppConstants.RoleName.fromString(r).getRole()))
                .toList();
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    /**
     * @return -- User entity that is retrieved from db
     * @throws UsernameNotFoundException -- throws UsernameNotFoundException
     */
    public User loggedInUser() {
        String username = ((org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return findByEmail(username);
    }

    public Page<TodoItem> findByTodoItems(UUID id, Pageable pageable) {
        return todoItemRepository.findAllByOwner(id, pageable);
    }

    public TodoItem findTodoItem(UUID id) throws ServerException {
        return todoItemRepository.findById(id)
                .orElseThrow(() -> new ServerException(AppConstants.OmaErrorMessageType.NOT_FOUND,
                        new String[] { id.toString() }, HttpStatus.NOT_FOUND));
    }

    public TodoItem createTodoItem(CreateTodoDto createTodo, User owner) {
        TodoItem todoItem = new TodoItem();
        todoItem.setDescription(createTodo.getDescription());
        todoItem.setFinished(false);
        todoItem.setOwner(owner.getId());
        todoItem.setId(UUID.randomUUID());
        return todoItemRepository.save(todoItem);
    }

    public TodoItem updateTodoItem(UUID id, UpdateTodoDto updateTodoDto) throws ServerException {
        TodoItem item = findTodoItem(id);
        if (updateTodoDto.getDescription() != null) {
            item.setDescription(updateTodoDto.getDescription());
        }
        if (updateTodoDto.getFinished() != null) {
            item.setFinished(updateTodoDto.getFinished());
        }
        return todoItemRepository.save(item);
    }

    public void deleteTodoItem(UUID id) throws ServerException {
        TodoItem item = findTodoItem(id);
        todoItemRepository.delete(item);
    }
}