package com.github.senocak.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.senocak.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({"user", "token"})
public class UserWrapperResponse extends BaseDto {
    @JsonProperty("user")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UserResponse userResponse;

    @Schema(example = "eyJraWQiOiJ...", description = "Jwt Token", requiredMode = Schema.RequiredMode.REQUIRED, name = "token", type = "String")
    private String token;

    public UserWrapperResponse(UserResponse userResponse, String token) {
        this.userResponse = userResponse;
        this.token = token;
    }

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}