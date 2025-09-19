package com.jira.jira.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    private String name;
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be between 2 and 10 characters")
    private String key;
    @JsonProperty("project_type")
    private String projectType = "SOFTWARE"; // Default to SOFTWARE
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("is_public")
    private Boolean isPublic = false; // Default to private
}