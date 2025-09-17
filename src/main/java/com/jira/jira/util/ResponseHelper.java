package com.jira.jira.util;

import com.jira.jira.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelper {

    // Success responses
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // Created responses
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        ApiResponse<T> response = ApiResponse.success(data, "Resource created successfully");
        response.setStatusCode(201);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        ApiResponse<T> response = ApiResponse.success(data, message);
        response.setStatusCode(201);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // No content responses
    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        ApiResponse<T> response = ApiResponse.success("Resource deleted successfully");
        response.setStatusCode(204);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent(String message) {
        ApiResponse<T> response = ApiResponse.success(message);
        response.setStatusCode(204);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    // Error responses
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.unauthorized(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.forbidden(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        ApiResponse<T> response = ApiResponse.error(message, "CONFLICT", 409);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> unprocessableEntity(String message) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.validationError(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        ApiResponse<T> response = ApiResponse.error(message, "INTERNAL_SERVER_ERROR", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
