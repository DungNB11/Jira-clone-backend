package com.jira.jira.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @JsonProperty("refresh_token")
    private String refreshToken;
}