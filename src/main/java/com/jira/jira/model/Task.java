package com.jira.jira.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "task")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task {
    @Id
    String id;
    String name;
    String description;
    @Field("workspace_id")
    String workspaceId;
    @Field("assignee_id")
    String assigneeId;
    @Field("project_id")
    String projectId;
    String status;
    @Field("created_by")
    String createdBy;
    @Field("due_at")
    LocalDateTime dueAt;
    @Field("created_at")
    LocalDateTime createdAt;
    @Field("updated_at")
    LocalDateTime updatedAt;
    @Field("is_active")
    Boolean isActive;
}
