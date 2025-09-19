package com.jira.jira.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TaskStatus {
    BACKLOG("backlog", "Backlog"),
    TODO("todo", "To Do"),
    IN_PROGRESS("inprogress", "In Progress"),
    IN_REVIEW("inreview", "In Review"),
    DONE("done", "Done");

    private final String value;
    private final String displayName;

    TaskStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid task status: " + value +
                ". Valid values are: backlog, todo, inprogress, inreview, done");
    }

    public static boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }

        try {
            fromValue(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get all valid status values for frontend validation
     */
    public static String[] getAllValues() {
        return new String[]{"backlog", "todo", "inprogress", "inreview", "done"};
    }

    /**
     * Check if status transition is valid (for business logic)
     */
    public boolean canTransitionTo(TaskStatus newStatus) {
        return newStatus != null;

        // Allow any transition for flexibility
        // Can add specific business rules here if needed
    }

    @Override
    public String toString() {
        return this.value;
    }
}
