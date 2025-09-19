package com.jira.jira.dto.websocket;

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
public class TaskUpdateEvent {

    @JsonProperty("event_type")
    private EventType eventType;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("task_name")
    private String taskName;

    @JsonProperty("old_status")
    private TaskStatus oldStatus;

    @JsonProperty("new_status")
    private TaskStatus newStatus;

    @JsonProperty("old_position")
    private Double oldPosition;

    @JsonProperty("new_position")
    private Double newPosition;

    @JsonProperty("assignee_id")
    private String assigneeId;

    @JsonProperty("assignee_name")
    private String assigneeName;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("updated_by_name")
    private String updatedByName;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("message")
    private String message;

    public enum EventType {
        TASK_CREATED,
        TASK_UPDATED,
        TASK_MOVED,
        TASK_ASSIGNED,
        TASK_DELETED,
        TASK_STATUS_CHANGED
    }

    // Helper methods to create common events
    public static TaskUpdateEvent taskCreated(String workspaceId, String projectId, String taskId,
                                              String taskName, String createdBy, String createdByName) {
        return TaskUpdateEvent.builder()
                .eventType(EventType.TASK_CREATED)
                .workspaceId(workspaceId)
                .projectId(projectId)
                .taskId(taskId)
                .taskName(taskName)
                .updatedBy(createdBy)
                .updatedByName(createdByName)
                .timestamp(LocalDateTime.now())
                .message(createdByName + " created task: " + taskName)
                .build();
    }

    public static TaskUpdateEvent taskMoved(String workspaceId, String projectId, String taskId,
                                            String taskName, TaskStatus oldStatus, TaskStatus newStatus,
                                            Double oldPosition, Double newPosition, String movedBy, String movedByName) {
        return TaskUpdateEvent.builder()
                .eventType(EventType.TASK_MOVED)
                .workspaceId(workspaceId)
                .projectId(projectId)
                .taskId(taskId)
                .taskName(taskName)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .oldPosition(oldPosition)
                .newPosition(newPosition)
                .updatedBy(movedBy)
                .updatedByName(movedByName)
                .timestamp(LocalDateTime.now())
                .message(movedByName + " moved " + taskName + " from " +
                        oldStatus.getDisplayName() + " to " + newStatus.getDisplayName())
                .build();
    }

    public static TaskUpdateEvent taskAssigned(String workspaceId, String projectId, String taskId,
                                               String taskName, String assigneeId, String assigneeName,
                                               String assignedBy, String assignedByName) {
        return TaskUpdateEvent.builder()
                .eventType(EventType.TASK_ASSIGNED)
                .workspaceId(workspaceId)
                .projectId(projectId)
                .taskId(taskId)
                .taskName(taskName)
                .assigneeId(assigneeId)
                .assigneeName(assigneeName)
                .updatedBy(assignedBy)
                .updatedByName(assignedByName)
                .timestamp(LocalDateTime.now())
                .message(assignedByName + " assigned " + taskName + " to " + assigneeName)
                .build();
    }

    public static TaskUpdateEvent taskUpdated(String workspaceId, String projectId, String taskId,
                                              String taskName, String updatedBy, String updatedByName) {
        return TaskUpdateEvent.builder()
                .eventType(EventType.TASK_UPDATED)
                .workspaceId(workspaceId)
                .projectId(projectId)
                .taskId(taskId)
                .taskName(taskName)
                .updatedBy(updatedBy)
                .updatedByName(updatedByName)
                .timestamp(LocalDateTime.now())
                .message(updatedByName + " updated task: " + taskName)
                .build();
    }
}

