package com.jira.jira.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWorkspaceRequest {
    @Size(min = 1, max = 100, message = "Workspace name must be between 1 and 100 characters")
    String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description;

    @JsonProperty("is_public")
    Boolean isPublic;

    @JsonProperty("avatar_url")
    String avatarUrl;

    @JsonProperty("join_url")
    String joinUrl;
}
