package com.jira.jira.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "workspace")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Workspace {
    @Id
    String id;
    @Field("owner_id")
    String ownerId;
    String name;
    @Field("join_url")
    String joinUrl;
    @Field("avatar_url")
    String avatarUrl;
    String description;
    @Field("join_users")
    List<String> joinUserIds; // List of user IDs
    @Field("created_at")
    LocalDateTime createdAt;
    @Field("updated_at")
    LocalDateTime updatedAt;
    @Field("is_active")
    Boolean isActive;
}
