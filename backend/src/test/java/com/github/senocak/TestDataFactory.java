package com.github.senocak;

import com.github.senocak.domain.TodoItem;
import com.github.senocak.domain.User;
import com.github.senocak.util.AppConstants;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {
    private static final String USER_NAME = "testUser";
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "password";

    public static User createTestUser() {
        User user = new User(USER_NAME, USER_EMAIL, USER_PASSWORD,
                List.of(AppConstants.RoleName.ROLE_USER.getRole(), AppConstants.RoleName.ROLE_ADMIN.getRole()),
                new Date());
        user.setId(UUID.randomUUID());
        return user;
    }

    public static TodoItem createTestTodo() {
        TodoItem todoItem = new TodoItem("description", UUID.randomUUID(), false);
        todoItem.setId(UUID.randomUUID());
        return todoItem;
    }
}
