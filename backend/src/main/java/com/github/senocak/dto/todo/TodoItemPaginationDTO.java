package com.github.senocak.dto.todo;

import com.github.senocak.domain.TodoItem;
import com.github.senocak.dto.PaginationResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public class TodoItemPaginationDTO extends PaginationResponse<TodoItem, TodoDto> {
    public TodoItemPaginationDTO(Page<TodoItem> pageModel, List<TodoDto> items, String sortBy, String sort) {
        super(pageModel, items, sortBy, sort);
    }
}
