package com.jira.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonIgnore
    private String refreshToken;
}
