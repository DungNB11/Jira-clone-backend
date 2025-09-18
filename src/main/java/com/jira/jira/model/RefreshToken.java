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
@Document(collection = "refresh_tokens")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken {
    @Id
    String id;
    @Field("token")
    String token;
    @Field("user_id")
    String userId;
    @Field("expires_at")
    LocalDateTime expiresAt;
    @Field("created_at")
    LocalDateTime createdAt;
    @Field("updated_at")
    LocalDateTime updatedAt;
    @Field("is_active")
    Boolean isActive;
    @Field("device_info")
    String deviceInfo; // Optional: để track device
    @Field("ip_address")
    String ipAddress; // Optional: để security audit
}
