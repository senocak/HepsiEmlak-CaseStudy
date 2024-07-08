package com.github.senocak.dto.auth;

import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest extends BaseDto {
    @NotBlank
    @Size(min = 3, max = 30)
    @Schema(example = "asenocak", description = "Username of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "email", type = "String")
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    @Schema(description = "Password of the user", name = "password", type = "String", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public @NotBlank @Size(min = 3, max = 30) String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Size(min = 3, max = 30) String email) {
        this.email = email;
    }

    public @NotBlank @Size(min = 6, max = 20) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(min = 6, max = 20) String password) {
        this.password = password;
    }
}
