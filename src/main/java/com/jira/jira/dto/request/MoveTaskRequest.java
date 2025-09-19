package com.jira.jira.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.jira.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveTaskRequest {

    @NotNull(message = "Target status is required")
    @JsonProperty("target_status")
    private TaskStatus targetStatus;

    @JsonProperty("target_position")
    private Double targetPosition;

    @JsonProperty("target_index")
    private Integer targetIndex;
}
