package com.jira.jira.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

@Data
@Document(collection = "tasks")
@CompoundIndexes({
        @CompoundIndex(name = "workspace_status_position_idx", def = "{'workspace_id' : 1, 'status' : 1, 'position' : 1}"),
        @CompoundIndex(name = "workspace_project_position_idx", def = "{'workspace_id' : 1, 'project_id' : 1, 'position' : 1}"),
        @CompoundIndex(name = "workspace_assignee_idx", def = "{'workspace_id' : 1, 'assignee_id' : 1}"),
        @CompoundIndex(name = "workspace_due_date_idx", def = "{'workspace_id' : 1, 'due_at' : 1}"),
        @CompoundIndex(name = "project_status_position_idx", def = "{'project_id' : 1, 'status' : 1, 'position' : 1}")
})
public class Task {

    @Id
    private String id;

    private String name;

    private String description;

    @Field("workspace_id")
    @Indexed
    private String workspaceId;

    @Field("assignee_id")
    private String assigneeId;

    @Field("project_id")
    @Indexed
    private String projectId;

    @Field("status")
    @JsonProperty("status")
    private TaskStatus status; // Using enum for type safety

    @Field("position")
    private Double position; // For ordering within status/column

    @Field("due_at")
    @Indexed
    private LocalDateTime dueAt;

    @Field("created_by")
    private String createdBy;

    @Field("created_at")
    @Indexed
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("is_active")
    @Indexed
    private boolean isActive = true;

    // Helper methods for business logic
    public boolean isOverdue() {
        return dueAt != null &&
                LocalDateTime.now().isAfter(dueAt) &&
                status != TaskStatus.DONE;
    }

    public boolean isAssignedTo(String userId) {
        return assigneeId != null && assigneeId.equals(userId);
    }

    public boolean belongsToWorkspace(String workspaceId) {
        return this.workspaceId != null && this.workspaceId.equals(workspaceId);
    }

    public boolean belongsToProject(String projectId) {
        return this.projectId != null && this.projectId.equals(projectId);
    }
}
