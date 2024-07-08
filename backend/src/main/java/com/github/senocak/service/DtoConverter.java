package com.github.senocak.service;

import com.github.senocak.dto.todo.TodoDto;
import com.github.senocak.dto.user.UserResponse;
import com.github.senocak.domain.TodoItem;
import com.github.senocak.domain.User;

public class DtoConverter {
    private DtoConverter(){}

    /**
     * @param user -- User object to convert to dto object
     * @return -- UserResponse object
     */
    public static UserResponse convertEntityToDto(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRoles(user.getRoles());
        if (user.getEmailActivatedAt() != null)
            userResponse.setEmailActivatedAt(user.getEmailActivatedAt().getTime());
        return userResponse;
    }

    /**
     * @param item -- object to convert to dto object
     * @param owner -- User object to convert to dto object
     * @return -- TodoDto object
     */
    public static TodoDto convertEntityToDto(TodoItem item, User owner){
        TodoDto dto = new TodoDto();
        dto.setId(item.getId().toString());
        dto.setDescription(item.getDescription());
        dto.setFinished(item.getFinished());
        dto.setOwner(convertEntityToDto(owner));
        return dto;
    }
}
