package com.jira.jira.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;
    private final Integer statusCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.statusCode = getDefaultStatusCode(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.statusCode = getDefaultStatusCode(errorCode);
    }

    public BusinessException(ErrorCode errorCode, Integer statusCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage, Integer statusCode) {
        super(customMessage);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    private Integer getDefaultStatusCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED, INVALID_CREDENTIALS, TOKEN_EXPIRED, TOKEN_INVALID -> 401;
            case FORBIDDEN, WORKSPACE_ACCESS_DENIED, PROJECT_ACCESS_DENIED, TASK_ACCESS_DENIED -> 403;
            case USER_NOT_FOUND, WORKSPACE_NOT_FOUND, PROJECT_NOT_FOUND, TASK_NOT_FOUND,
                 NOTIFICATION_NOT_FOUND, KANBAN_COLUMN_NOT_FOUND -> 404;
            case VALIDATION_ERROR, INVALID_TASK_STATUS -> 422;
            case USER_ALREADY_EXISTS, WORKSPACE_ALREADY_EXISTS, PROJECT_ALREADY_EXISTS -> 409;
            case INTERNAL_SERVER_ERROR, DATABASE_ERROR -> 500;
            default -> 400;
        };
    }
}
