package com.jira.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    boolean success;
    String message;
    T data;
    String error;
    String errorCode;
    LocalDateTime timestamp;
    Integer statusCode;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .timestamp(LocalDateTime.now())
                .statusCode(400)
                .build();
    }

    public static <T> ApiResponse<T> error(String error, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .statusCode(400)
                .build();
    }

    public static <T> ApiResponse<T> error(String error, String errorCode, Integer statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .errorCode(errorCode)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Not found response
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .errorCode("NOT_FOUND")
                .statusCode(404)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Unauthorized response
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .errorCode("UNAUTHORIZED")
                .statusCode(401)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Forbidden response
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .errorCode("FORBIDDEN")
                .statusCode(403)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Validation error response
    public static <T> ApiResponse<T> validationError(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .errorCode("VALIDATION_ERROR")
                .statusCode(422)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
