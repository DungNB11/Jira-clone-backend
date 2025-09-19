package com.jira.jira.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "workspace")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Workspace {
    @Id
    private String id;
    @Field("owner_id")
    private String ownerId;
    private String name;
    private String description;
    @Field("is_public")
    private boolean isPublic;
    @Field("member_ids")
    private List<String> memberIds;
    @Field("avatar_url")
    private String avatarUrl;
    @Field("join_url")
    private String joinUrl;
    @Field("created_at")
    private LocalDateTime createdAt;
    @Field("updated_at")
    private LocalDateTime updatedAt;
    @Field("is_active")
    private boolean isActive;
}
