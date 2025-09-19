package com.jira.jira.controller;

import com.jira.jira.dto.common.PaginationRequest;
import com.jira.jira.dto.common.PaginationResponse;
import com.jira.jira.dto.common.SearchFilterRequest;
import com.jira.jira.dto.request.CreateTaskRequest;
import com.jira.jira.dto.request.MoveTaskRequest;
import com.jira.jira.dto.request.UpdateTaskRequest;
import com.jira.jira.dto.response.ApiResponse;
import com.jira.jira.dto.response.TaskResponse;
import com.jira.jira.model.TaskStatus;
import com.jira.jira.service.AuthService;
import com.jira.jira.service.TaskService;
import com.jira.jira.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final AuthService authService;

    // ========== CRUD OPERATIONS ==========

    /**
     * Create task in workspace
     * POST /api/workspaces/{workspaceId}/tasks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable String workspaceId,
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        TaskResponse response = taskService.createTask(request, workspaceId, userId);
        return ResponseHelper.created(response, "Task created successfully");
    }

    /**
     * Get tasks in workspace with filtering and pagination
     * GET /api/workspaces/{workspaceId}/tasks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<TaskResponse>>> getWorkspaceTasks(
            @PathVariable String workspaceId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignee_id,
            @RequestParam(required = false) String project_id,
            @RequestParam(required = false) String created_by,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort_by,
            @RequestParam(required = false) String sort_direction,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();

        // Build search filter request
        Map<String, Object> filters = new HashMap<>();
        if (status != null) filters.put("status", status);
        if (assignee_id != null) filters.put("assignee_id", assignee_id);
        if (project_id != null) filters.put("project_id", project_id);
        if (created_by != null) filters.put("created_by", created_by);

        SearchFilterRequest request = SearchFilterRequest.builder()
                .search(search)
                .pagination(PaginationRequest.builder()
                        .page(page)
                        .size(size)
                        .sortBy(sort_by)
                        .sortDirection(sort_direction != null ?
                                PaginationRequest.SortDirection.valueOf(sort_direction.toUpperCase()) :
                                PaginationRequest.SortDirection.ASC)
                        .build())
                .filters(filters)
                .build();

        PaginationResponse<TaskResponse> tasks = taskService.getTasksByWorkspace(workspaceId, userId, request);
        return ResponseHelper.ok(tasks, "Tasks retrieved successfully");
    }

    /**
     * Get task by ID
     * GET /api/workspaces/{workspaceId}/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable String workspaceId,
            @PathVariable String taskId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        TaskResponse task = taskService.getTaskById(taskId, userId);
        return ResponseHelper.ok(task, "Task retrieved successfully");
    }

    /**
     * Update task
     * PUT /api/workspaces/{workspaceId}/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable String workspaceId,
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        TaskResponse response = taskService.updateTask(taskId, request, userId);
        return ResponseHelper.ok(response, "Task updated successfully");
    }

    /**
     * Delete task
     * DELETE /api/workspaces/{workspaceId}/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable String workspaceId,
            @PathVariable String taskId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        taskService.deleteTask(taskId, userId);
        return ResponseHelper.ok(null, "Task deleted successfully");
    }

    // ========== MOVE TASK API (DRAG & DROP) ==========

    /**
     * Move task to new position (for drag & drop)
     * PUT /api/workspaces/{workspaceId}/tasks/{taskId}/move
     */
    @PutMapping("/{taskId}/move")
    public ResponseEntity<ApiResponse<TaskResponse>> moveTask(
            @PathVariable String workspaceId,
            @PathVariable String taskId,
            @Valid @RequestBody MoveTaskRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        TaskResponse response = taskService.moveTask(taskId, request, workspaceId, userId);
        return ResponseHelper.ok(response, "Task moved successfully");
    }

    // ========== VIEW-SPECIFIC ENDPOINTS ==========

    /**
     * Get tasks for Kanban view (ordered by position)
     * GET /api/workspaces/{workspaceId}/tasks/kanban
     */
    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksForKanban(
            @PathVariable String workspaceId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        List<TaskResponse> tasks = taskService.getTasksForKanbanView(workspaceId, userId);
        return ResponseHelper.ok(tasks, "Kanban tasks retrieved successfully");
    }

    /**
     * Get tasks by status for Kanban columns
     * GET /api/workspaces/{workspaceId}/tasks/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByStatus(
            @PathVariable String workspaceId,
            @PathVariable String status,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        TaskStatus taskStatus = TaskStatus.fromValue(status);
        List<TaskResponse> tasks = taskService.getTasksByStatus(workspaceId, taskStatus, userId);
        return ResponseHelper.ok(tasks, "Tasks by status retrieved successfully");
    }

    /**
     * Get tasks for Calendar view (filtered by date range)
     * GET /api/workspaces/{workspaceId}/tasks/calendar?startDate=...&endDate=...
     */
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksForCalendar(
            @PathVariable String workspaceId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);

        List<TaskResponse> tasks = taskService.getTasksForCalendarView(workspaceId, start, end, userId);
        return ResponseHelper.ok(tasks, "Calendar tasks retrieved successfully");
    }

    // ========== SPECIAL QUERIES ==========

    /**
     * Get overdue tasks in workspace
     * GET /api/workspaces/{workspaceId}/tasks/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @PathVariable String workspaceId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        List<TaskResponse> tasks = taskService.getOverdueTasks(workspaceId, userId);
        return ResponseHelper.ok(tasks, "Overdue tasks retrieved successfully");
    }

    /**
     * Get tasks assigned to current user (within workspace)
     * GET /api/workspaces/{workspaceId}/tasks/assigned
     */
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAssignedTasks(
            @PathVariable String workspaceId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        List<TaskResponse> tasks = taskService.getAssignedTasks(userId);
        return ResponseHelper.ok(tasks, "Assigned tasks retrieved successfully");
    }

    // ========== UTILITY ENDPOINTS ==========

    /**
     * Get valid task statuses for frontend validation
     * GET /api/workspaces/{workspaceId}/tasks/statuses
     */
    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getValidStatuses(
            @PathVariable String workspaceId) {
        Map<String, Object> response = new HashMap<>();
        response.put("statuses", TaskStatus.getAllValues());
        response.put("default_status", TaskStatus.TODO.getValue());

        return ResponseHelper.ok(response, "Valid task statuses retrieved successfully");
    }
}

// ========== SEPARATE CONTROLLERS FOR DIFFERENT CONTEXTS ==========

/**
 * Project-specific task operations
 */
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
class ProjectTaskController {

    private final TaskService taskService;
    private final AuthService authService;

    /**
     * Get tasks by project
     * GET /api/projects/{projectId}/tasks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getProjectTasks(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        List<TaskResponse> tasks = taskService.getTasksByProject(projectId, userId);
        return ResponseHelper.ok(tasks, "Project tasks retrieved successfully");
    }
}

/**
 * Global task operations (not workspace-specific)
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
class GlobalTaskController {

    private final TaskService taskService;
    private final AuthService authService;

    /**
     * Get all tasks assigned to current user (across all workspaces)
     * GET /api/tasks/assigned
     */
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllAssignedTasks(
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        List<TaskResponse> tasks = taskService.getAssignedTasks(userId);
        return ResponseHelper.ok(tasks, "All assigned tasks retrieved successfully");
    }
}