package com.jira.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    @JsonProperty("access_token")
    private String accessToken;

    private UserInfoResponse user;

    @JsonIgnore
    private String refreshToken;
}
