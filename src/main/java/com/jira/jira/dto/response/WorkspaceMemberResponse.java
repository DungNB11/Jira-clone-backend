package com.jira.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkspaceMemberResponse {
    @JsonProperty("user_id")
    String userId;

    @JsonProperty("email")
    String email;

    @JsonProperty("name")
    String name;

    @JsonProperty("role")
    String role;

    @JsonProperty("joined_at")
    LocalDateTime joinedAt;

    @JsonProperty("is_active")
    Boolean isActive;
}
