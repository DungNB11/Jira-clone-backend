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
public class ProjectPermissions {

    @JsonProperty("can_edit")
    private Boolean canEdit;

    @JsonProperty("can_delete")
    private Boolean canDelete;

    @JsonProperty("can_add_members")
    private Boolean canAddMembers;

    @JsonProperty("can_remove_members")
    private Boolean canRemoveMembers;

    @JsonProperty("can_create_tasks")
    private Boolean canCreateTasks;

    @JsonProperty("can_manage_columns")
    private Boolean canManageColumns;

    @JsonProperty("can_view_analytics")
    private Boolean canViewAnalytics;

    @JsonProperty("can_manage_settings")
    private Boolean canManageSettings;
}

