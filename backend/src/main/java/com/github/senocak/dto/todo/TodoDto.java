package com.github.senocak.dto.todo;

import com.github.senocak.dto.BaseDto;
import com.github.senocak.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class TodoDto extends BaseDto {
    @Schema(example = "1234-1234-1234-1234", description = "Id of the todo", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "String")
    private String id;

    @Schema(example = "Lorem", description = "Description of the todo", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "String")
    @Size(min = 5, max = 250)
    private String description;

    @Schema(example = "false", description = "Is finished?", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "Boolean")
    private Boolean finished;

    private UserResponse owner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @Size(min = 5, max = 250) String getDescription() {
        return description;
    }

    public void setDescription(@Size(min = 5, max = 250) String description) {
        this.description = description;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public UserResponse getOwner() {
        return owner;
    }

    public void setOwner(UserResponse owner) {
        this.owner = owner;
    }
}
