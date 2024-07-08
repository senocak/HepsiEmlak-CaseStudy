package com.github.senocak.dto.todo;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class UpdateTodoDto extends BaseDto {
    @Schema(example = "Lorem", description = "Description of the todo", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "String")
    @Size(min = 5, max = 250)
    private String description;

    @Schema(example = "false", description = "Is finished?", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "Boolean")
    private Boolean finished;

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
}