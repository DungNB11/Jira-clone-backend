package com.jira.jira.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "notification")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    String id;
    String name;
    String url;
    @Field("sender_id")
    String senderId;
    @Field("receiver_id")
    String receiverId;
    @Field("is_seen")
    Boolean isSeen;
    @Field("created_at")
    String createdAt;
    @Field("updated_at")
    String updatedAt;
    @Field("is_active")
    Boolean isActive;
}
