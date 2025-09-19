package com.jira.jira.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {

    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @JsonProperty("description")
    private String description;

    @JsonProperty("project_type")
    private String projectType;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("is_public")
    private Boolean isPublic;
}

