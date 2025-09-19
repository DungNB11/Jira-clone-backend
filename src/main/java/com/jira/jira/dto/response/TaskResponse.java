package com.jira.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jira.jira.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("workspace")
    private WorkspaceInfo workspace;

    @JsonProperty("assignee_id")
    private String assigneeId;

    @JsonProperty("assignee")
    private UserInfo assignee;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("project")
    private ProjectInfo project;

    @JsonProperty("status")
    private TaskStatus status;

    @JsonProperty("position")
    private Double position;

    @JsonProperty("due_at")
    private LocalDateTime dueAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_user")
    private UserInfo createdUser;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("is_overdue")
    private Boolean isOverdue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        @JsonProperty("id")
        private String id;

        @JsonProperty("email")
        private String email;

        @JsonProperty("display_name")
        private String displayName;

        @JsonProperty("photo_url")
        private String photoUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkspaceInfo {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectInfo {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("key")
        private String key;
    }
}
