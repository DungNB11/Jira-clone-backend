package com.jira.jira.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "projects")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project {

    @Id
    String id;
    String name;
    String description;
    String key; // Project key (e.g., "TEST", "PROJ")
    @Field("project_type")
    String projectType; // SOFTWARE, BUSINESS, etc.
    @Field("workspace_id")
    String workspaceId;
    @Field("owner_id")
    String ownerId;
    @Field("member_ids")
    List<String> memberIds; // List of user IDs who are members
    @Field("is_public")
    boolean isPublic;
    @Field("avatar_url")
    String avatarUrl;
    @Field("created_at")
    LocalDateTime createdAt;
    @Field("updated_at")
    LocalDateTime updatedAt;
    @Field("is_active")
    boolean isActive;
}
