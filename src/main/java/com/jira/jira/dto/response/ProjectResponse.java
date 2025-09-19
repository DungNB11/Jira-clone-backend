package com.jira.jira.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("key")
    private String key;

    @JsonProperty("project_type")
    private String projectType;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("owner_id")
    private String ownerId;

    @JsonProperty("is_public")
    private Boolean isPublic;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("member_count")
    private Integer memberCount;

    @JsonProperty("task_count")
    private Integer taskCount;

    @JsonProperty("completed_task_count")
    private Integer completedTaskCount;

    @JsonProperty("progress_percentage")
    private Double progressPercentage;

    @JsonProperty("user_role")
    private String userRole;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("members")
    private List<ProjectMemberResponse> members;

    @JsonProperty("permissions")
    private ProjectPermissions permissions;
}

