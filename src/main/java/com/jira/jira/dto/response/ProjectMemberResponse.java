package com.jira.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberResponse {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("role")
    private String role;

    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;

    @JsonProperty("is_active")
    private Boolean isActive;
}
