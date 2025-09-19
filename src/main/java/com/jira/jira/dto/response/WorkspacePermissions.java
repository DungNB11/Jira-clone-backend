package com.jira.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspacePermissions {

    @JsonProperty("can_edit")
    private Boolean canEdit;

    @JsonProperty("can_delete")
    private Boolean canDelete;

    @JsonProperty("can_add_members")
    private Boolean canAddMembers;

    @JsonProperty("can_remove_members")
    private Boolean canRemoveMembers;

    @JsonProperty("can_create_projects")
    private Boolean canCreateProjects;

    @JsonProperty("can_manage_settings")
    private Boolean canManageSettings;

    @JsonProperty("can_view_analytics")
    private Boolean canViewAnalytics;
}
