package com.github.senocak.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonPropertyOrder({"name", "email", "roles", "emailActivatedAt"})
public class UserResponse extends BaseDto {
    @JsonProperty("name")
    @Schema(example = "Lorem Ipsum", description = "Name of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "name", type = "String")
    private String name;

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "email", type = "String")
    private String email;

    @Schema(example = "asenocak", description = "Username of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "username", type = "String")
    private String username;

    @ArraySchema(schema = @Schema(example = "ROLE_USER", description = "Roles of the user", requiredMode = Schema.RequiredMode.REQUIRED, name = "roles"))
    private List<String> roles;

    @Schema(example = "1253123123", description = "Email activation datetime", name = "emailActivatedAt", type = "Long")
    private Long emailActivatedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Long getEmailActivatedAt() {
        return emailActivatedAt;
    }

    public void setEmailActivatedAt(Long emailActivatedAt) {
        this.emailActivatedAt = emailActivatedAt;
    }
}
