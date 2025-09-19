package com.jira.jira.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.jira.model.TaskStatus;
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
public class UpdateTaskRequest {

    @Size(min = 1, max = 200, message = "Task name must be between 1 and 200 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;

    @JsonProperty("assignee_id")
    private String assigneeId;

    @JsonProperty("status")
    private TaskStatus status;

    @JsonProperty("position")
    private Double position;

    @JsonProperty("due_at")
    private LocalDateTime dueAt;
}
