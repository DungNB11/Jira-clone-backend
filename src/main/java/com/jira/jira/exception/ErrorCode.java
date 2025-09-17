package com.jira.jira.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // General errors
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal server error occurred"),
    INVALID_REQUEST("INVALID_REQUEST", "Invalid request parameters"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed"),

    // Authentication errors
    UNAUTHORIZED("UNAUTHORIZED", "Authentication required"),
    FORBIDDEN("FORBIDDEN", "Access denied"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid email or password"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "JWT token has expired"),
    TOKEN_INVALID("TOKEN_INVALID", "JWT token is invalid"),

    // User errors
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User already exists with this email"),
    USER_INACTIVE("USER_INACTIVE", "User account is inactive"),

    // Workspace errors
    WORKSPACE_NOT_FOUND("WORKSPACE_NOT_FOUND", "Workspace not found"),
    WORKSPACE_ACCESS_DENIED("WORKSPACE_ACCESS_DENIED", "Access denied to workspace"),
    WORKSPACE_ALREADY_EXISTS("WORKSPACE_ALREADY_EXISTS", "Workspace with this name already exists"),

    // Project errors
    PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", "Project not found"),
    PROJECT_ACCESS_DENIED("PROJECT_ACCESS_DENIED", "Access denied to project"),
    PROJECT_ALREADY_EXISTS("PROJECT_ALREADY_EXISTS", "Project with this name already exists"),

    // Task errors
    TASK_NOT_FOUND("TASK_NOT_FOUND", "Task not found"),
    TASK_ACCESS_DENIED("TASK_ACCESS_DENIED", "Access denied to task"),
    INVALID_TASK_STATUS("INVALID_TASK_STATUS", "Invalid task status"),

    // Notification errors
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND", "Notification not found"),

    // Kanban errors
    KANBAN_COLUMN_NOT_FOUND("KANBAN_COLUMN_NOT_FOUND", "Kanban column not found"),

    // Database errors
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed"),
    CONSTRAINT_VIOLATION("CONSTRAINT_VIOLATION", "Database constraint violation");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
