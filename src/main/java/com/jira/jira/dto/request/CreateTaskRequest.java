package com.jira.jira.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.jira.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotBlank(message = "Task name is required")
    @Size(min = 1, max = 200, message = "Task name must be between 1 and 200 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("assignee_id")
    private String assigneeId;

    @JsonProperty("status")
    private TaskStatus status; // Optional, defaults to TODO

    @JsonProperty("position")
    private Double position; // Optional, will be calculated if not provided

    @JsonProperty("due_at")
    private LocalDateTime dueAt;
}