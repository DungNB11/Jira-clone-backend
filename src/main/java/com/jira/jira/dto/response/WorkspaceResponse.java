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
public class WorkspaceResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_public")
    private Boolean isPublic;

    @JsonProperty("owner_id")
    private String ownerId;

    @JsonProperty("member_count")
    private Integer memberCount;

    @JsonProperty("project_count")
    private Integer projectCount;

    @JsonProperty("task_count")
    private Integer taskCount;

    @JsonProperty("active_member_count")
    private Integer activeMemberCount;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("join_url")
    private String joinUrl;

    @JsonProperty("user_role")
    private String userRole;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("members")
    private List<WorkspaceMemberResponse> members;

    @JsonProperty("permissions")
    private WorkspacePermissions permissions;
}

