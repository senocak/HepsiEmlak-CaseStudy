package com.github.senocak.dto.user;

import com.github.senocak.dto.BaseDto;
import com.github.senocak.util.validator.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@PasswordMatches
public class UpdateUserDto extends BaseDto {
    @Size(min = 4, max = 40)
    @Schema(example = "Anil", description = "Name", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "String")
    private String name;

    @Size(min = 6, max = 20)
    @Schema(example = "Anil123", description = "Password", requiredMode = Schema.RequiredMode.REQUIRED, name = "password", type = "String")
    private String password;

    @Size(min = 6, max = 20)
    @Schema(example = "Anil123", description = "Password confirmation", requiredMode = Schema.RequiredMode.REQUIRED, name = "password", type = "String")
    private String password_confirmation;

    public @Size(min = 4, max = 40) String getName() {
        return name;
    }

    public void setName(@Size(min = 4, max = 40) String name) {
        this.name = name;
    }

    public @Size(min = 6, max = 20) String getPassword() {
        return password;
    }

    public void setPassword(@Size(min = 6, max = 20) String password) {
        this.password = password;
    }

    public @Size(min = 6, max = 20) String getPassword_confirmation() {
        return password_confirmation;
    }

    public void setPassword_confirmation(@Size(min = 6, max = 20) String password_confirmation) {
        this.password_confirmation = password_confirmation;
    }
}
