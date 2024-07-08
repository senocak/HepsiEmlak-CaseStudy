package com.github.senocak.dto.auth;

import com.github.senocak.util.validator.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotNull
    @Size(min = 4, max = 40)
    @Schema(example = "Lorem Ipsum", description = "Name of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "String")
    private String name;

    @NotNull
    @ValidEmail
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "email", type = "String")
    private String email;

    @NotNull
    @Size(min = 6, max = 20)
    @Schema(example = "asenocak123", description = "Password of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "password", type = "String")
    private String password;

    public @NotNull @Size(min = 4, max = 40) String getName() {
        return name;
    }

    public void setName(@NotNull @Size(min = 4, max = 40) String name) {
        this.name = name;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public @NotNull @Size(min = 6, max = 20) String getPassword() {
        return password;
    }

    public void setPassword(@NotNull @Size(min = 6, max = 20) String password) {
        this.password = password;
    }
}