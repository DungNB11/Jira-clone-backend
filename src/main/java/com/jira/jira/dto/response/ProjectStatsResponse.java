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
public class ProjectStatsResponse {

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("total_tasks")
    private Integer totalTasks;

    @JsonProperty("completed_tasks")
    private Integer completedTasks;

    @JsonProperty("in_progress_tasks")
    private Integer inProgressTasks;

    @JsonProperty("todo_tasks")
    private Integer todoTasks;

    @JsonProperty("progress_percentage")
    private Double progressPercentage;

    @JsonProperty("total_members")
    private Integer totalMembers;

    @JsonProperty("active_members")
    private Integer activeMembers;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("last_activity")
    private LocalDateTime lastActivity;

    @JsonProperty("task_completion_rate")
    private Double taskCompletionRate;

    @JsonProperty("average_task_completion_time")
    private Double averageTaskCompletionTime; // in hours
}
