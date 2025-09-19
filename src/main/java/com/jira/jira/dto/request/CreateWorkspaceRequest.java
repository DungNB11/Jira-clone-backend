package com.jira.jira.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class CreateWorkspaceRequest {
    @NotBlank(message = "Workspace name is required")
    @Size(min = 1, max = 100, message = "Workspace name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Boolean isPublic = false;
}
