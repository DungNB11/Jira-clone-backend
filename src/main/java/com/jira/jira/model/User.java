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
@Document(collection = "users")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    String id;
    String name;
    String email;
    String password;
    @Field("phone_number")
    String phoneNumber;
    @Field("photo_url")
    String photoUrl;
    @Field("created_at")
    LocalDateTime createdAt;
    @Field("updated_at")
    LocalDateTime updatedAt;
    @Field("is_active")
    Boolean isActive;
}
