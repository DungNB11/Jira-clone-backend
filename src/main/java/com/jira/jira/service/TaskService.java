package com.jira.jira.service;

import com.jira.jira.dto.common.PaginationRequest;
import com.jira.jira.dto.common.PaginationResponse;
import com.jira.jira.dto.common.SearchFilterRequest;
import com.jira.jira.dto.request.CreateTaskRequest;
import com.jira.jira.dto.request.MoveTaskRequest;
import com.jira.jira.dto.request.UpdateTaskRequest;
import com.jira.jira.dto.response.TaskResponse;
import com.jira.jira.dto.websocket.TaskUpdateEvent;
import com.jira.jira.exception.BusinessException;
import com.jira.jira.exception.ErrorCode;
import com.jira.jira.mapper.TaskMapper;
import com.jira.jira.model.*;
import com.jira.jira.repository.ProjectRepository;
import com.jira.jira.repository.TaskRepository;
import com.jira.jira.repository.UserRepository;
import com.jira.jira.repository.WorkspaceRepository;
import com.jira.jira.util.PaginationUtils;
import com.jira.jira.util.SearchFilterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService extends BaseService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final WebSocketService webSocketService;

    /**
     * Create task in workspace
     */
    public TaskResponse createTask(CreateTaskRequest request, String workspaceId, String userId) {
        // Validate workspace access
        Workspace workspace = workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId).orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        // Validate project if specified
        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndWorkspaceIdAndIsActive(request.getProjectId(), workspaceId, true).orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        }

        // Validate assignee if specified
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        // Determine task status
        TaskStatus taskStatus = request.getStatus() != null ? request.getStatus() : TaskStatus.TODO;
        validateTaskStatus(taskStatus);

        // Calculate position for new task
        Double position = request.getPosition() != null ? request.getPosition() : calculateNewPosition(workspaceId, taskStatus);

        // Create task
        Task task = new Task();
        task.setName(request.getName().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        task.setWorkspaceId(workspaceId);
        task.setProjectId(request.getProjectId());
        task.setAssigneeId(request.getAssigneeId());
        task.setStatus(taskStatus);
        task.setPosition(position);
        task.setDueAt(request.getDueAt());
        task.setCreatedBy(userId);
        task.setActive(true);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        User createdByUser = userRepository.findById(userId).orElse(null);
        String createdByName = createdByUser != null ? createdByUser.getName() : "Unknown";

        // Send WebSocket notification
        TaskUpdateEvent event = TaskUpdateEvent.taskCreated(workspaceId, request.getProjectId(), savedTask.getId(), savedTask.getName(), userId, createdByName);

        webSocketService.sendTaskUpdateToWorkspace(workspaceId, event);
        if (request.getProjectId() != null) {
            webSocketService.sendTaskUpdateToProject(request.getProjectId(), event);
        }
        webSocketService.sendActivityUpdate(workspaceId, event);
        return taskMapper.toTaskResponse(savedTask);


    }

    /**
     * Get tasks by workspace with filtering and pagination
     */
    @Transactional(readOnly = true)
    public PaginationResponse<TaskResponse> getTasksByWorkspace(String workspaceId, String userId, SearchFilterRequest request) {
        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId).orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        List<Task> tasks = taskRepository.findByWorkspaceIdAndIsActive(workspaceId, true);

        // Define search fields
        List<Function<Task, String>> searchFields = List.of(Task::getName, Task::getDescription);

        // Define custom filters
        List<Predicate<Task>> customFilters = List.of(
                // Status filter
                task -> {
                    String status = request.getFilterAsString("status");
                    return status == null || task.getStatus().getValue().equalsIgnoreCase(status);
                },
                // Assignee filter
                task -> {
                    String assigneeId = request.getFilterAsString("assignee_id");
                    return assigneeId == null || (task.getAssigneeId() != null && task.getAssigneeId().equals(assigneeId));
                },
                // Project filter
                task -> {
                    String projectId = request.getFilterAsString("project_id");
                    return projectId == null || (task.getProjectId() != null && task.getProjectId().equals(projectId));
                },
                // Created by filter
                task -> {
                    String createdBy = request.getFilterAsString("created_by");
                    return createdBy == null || task.getCreatedBy().equals(createdBy);
                });

        // Apply search filter
        if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
            tasks = SearchFilterUtils.applyMultiFieldSearch(tasks.stream(), request, searchFields).collect(Collectors.toList());
        }

        // Apply custom filters
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            for (Predicate<Task> filter : customFilters) {
                tasks = tasks.stream().filter(filter).collect(Collectors.toList());
            }
        }

        // Apply sorting
        PaginationRequest pagination = request.getPagination();
        if (pagination != null && pagination.getSortBy() != null && !pagination.getSortBy().trim().isEmpty()) {
            tasks = PaginationUtils.sortList(tasks, pagination, task -> getTaskSortKey(task, pagination));
        }

        // Apply pagination
        List<Task> paginatedTasks = PaginationUtils.paginateList(tasks, pagination);

        // Create pagination response
        PaginationResponse<Task> paginationResponse = PaginationResponse.<Task>builder().content(paginatedTasks).page(pagination.getPage()).size(pagination.getSize()).totalElements((long) tasks.size()).totalPages(PaginationUtils.calculateTotalPages(tasks.size(), pagination.getSize())).first(pagination.getPage() == 0).last(pagination.getPage() >= PaginationUtils.calculateTotalPages(tasks.size(), pagination.getSize()) - 1).numberOfElements(paginatedTasks.size()).empty(paginatedTasks.isEmpty()).sortBy(pagination.getSortBy()).sortDirection(String.valueOf(pagination.getSortDirection())).build();

        // Convert to response DTOs
        List<TaskResponse> taskResponses = paginatedTasks.stream().map(taskMapper::toTaskResponse).collect(Collectors.toList());

        return PaginationResponse.<TaskResponse>builder().content(taskResponses).page(paginationResponse.getPage()).size(paginationResponse.getSize()).totalElements(paginationResponse.getTotalElements()).totalPages(paginationResponse.getTotalPages()).first(paginationResponse.getFirst()).last(paginationResponse.getLast()).numberOfElements(paginationResponse.getNumberOfElements()).empty(paginationResponse.getEmpty()).sortBy(paginationResponse.getSortBy()).sortDirection(paginationResponse.getSortDirection()).build();
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(String taskId, String userId) {
        Task task = taskRepository.findByIdAndIsActive(taskId, true).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        // Validate user has access to workspace
        workspaceRepository.findByIdAndUserIdAndIsActive(task.getWorkspaceId(), userId).orElseThrow(() -> new BusinessException(ErrorCode.TASK_ACCESS_DENIED));

        return taskMapper.toTaskResponse(task);
    }

    /**
     * Update task
     */
    public TaskResponse updateTask(String taskId, UpdateTaskRequest request, String userId) {
        Task task = taskRepository.findByIdAndIsActive(taskId, true).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        // Validate user has access to workspace
        workspaceRepository.findByIdAndUserIdAndIsActive(task.getWorkspaceId(), userId).orElseThrow(() -> new BusinessException(ErrorCode.TASK_ACCESS_DENIED));

        // Update fields
        if (request.getName() != null) {
            task.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription().trim());
        }

        if (request.getAssigneeId() != null) {
            // Validate assignee exists
            if (!request.getAssigneeId().isEmpty()) {
                userRepository.findById(request.getAssigneeId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            }
            task.setAssigneeId(request.getAssigneeId().isEmpty() ? null : request.getAssigneeId());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getDueAt() != null) {
            task.setDueAt(request.getDueAt());
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(savedTask);
    }

    /**
     * Delete task (soft delete)
     */
    public void deleteTask(String taskId, String userId) {
        Task task = taskRepository.findByIdAndIsActive(taskId, true).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        // Validate user has access to workspace
        workspaceRepository.findByIdAndUserIdAndIsActive(task.getWorkspaceId(), userId).orElseThrow(() -> new BusinessException(ErrorCode.TASK_ACCESS_DENIED));

        task.setActive(false);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    /**
     * Get tasks by project
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(String projectId, String userId) {
        // Validate project access
        Project project = projectRepository.findByIdAndIsActive(projectId, true).orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(project.getWorkspaceId(), userId).orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));

        List<Task> tasks = taskRepository.findByProjectIdAndIsActive(projectId, true);
        return taskMapper.toTaskResponseList(tasks);
    }

    /**
     * Get tasks assigned to user
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAssignedTasks(String userId) {
        List<Task> tasks = taskRepository.findByAssigneeIdAndIsActive(userId, true);
        return taskMapper.toTaskResponseList(tasks);
    }

    /**
     * Get overdue tasks in workspace
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(String workspaceId, String userId) {
        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId).orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        List<Task> tasks = taskRepository.findOverdueTasksByWorkspace(workspaceId, LocalDateTime.now(), TaskStatus.DONE, true);
        return taskMapper.toTaskResponseList(tasks);
    }

    /**
     * Get sort key for task sorting
     */
    private Comparable getTaskSortKey(Task task, PaginationRequest pagination) {
        if (pagination == null || pagination.getSortBy() == null) {
            return task.getName();
        }

        String sortBy = pagination.getSortBy().toLowerCase();
        switch (sortBy) {
            case "name":
                return task.getName();
            case "status":
                return task.getStatus() != null ? task.getStatus().getValue() : "";
            case "position":
                return task.getPosition();
            case "due_at":
                return task.getDueAt();
            case "created_at":
                return task.getCreatedAt();
            case "updated_at":
                return task.getUpdatedAt();
            default:
                return task.getName();
        }
    }

    // ========== POSITION CALCULATION METHODS ==========

    /**
     * Calculate next position for a new task in a specific status
     */
    private Double calculateNewPosition(String workspaceId, TaskStatus status) {
        Optional<Task> lastTask = taskRepository.findFirstByWorkspaceIdAndStatusAndIsActiveOrderByPositionDesc(workspaceId, status, true);

        if (lastTask.isEmpty()) {
            return 1000.0; // First task in this status
        }

        return lastTask.get().getPosition() + 1000.0; // Add 1000 for spacing
    }

    /**
     * Calculate position when moving task to a specific index in a column
     */
    private Double calculatePositionAtIndex(String workspaceId, TaskStatus status, int targetIndex) {
        List<Task> tasksInColumn = taskRepository.findByWorkspaceIdAndStatusAndIsActiveOrderByPositionAsc(workspaceId, status, true);

        if (tasksInColumn.isEmpty()) {
            return 1000.0; // First task in column
        }

        if (targetIndex <= 0) {
            // Move to top
            return tasksInColumn.get(0).getPosition() - 1000.0;
        }

        if (targetIndex >= tasksInColumn.size()) {
            // Move to bottom
            return tasksInColumn.get(tasksInColumn.size() - 1).getPosition() + 1000.0;
        }

        // Move between two tasks
        Double prevPosition = tasksInColumn.get(targetIndex - 1).getPosition();
        Double nextPosition = tasksInColumn.get(targetIndex).getPosition();
        return (prevPosition + nextPosition) / 2.0;
    }

    /**
     * Calculate position when moving task between two existing positions
     */
    private Double calculatePositionBetween(Double prevPosition, Double nextPosition) {
        if (prevPosition == null && nextPosition == null) {
            return 1000.0;
        }

        if (prevPosition == null) {
            return nextPosition - 1000.0;
        }

        if (nextPosition == null) {
            return prevPosition + 1000.0;
        }

        return (prevPosition + nextPosition) / 2.0;
    }

    // ========== MOVE TASK API (DRAG & DROP) ==========

    /**
     * Move task to a new position (for drag & drop)
     */
    public TaskResponse moveTask(String taskId, MoveTaskRequest request, String workspaceId, String userId) {
        // Validate task access
        Task task = taskRepository.findByIdAndWorkspaceIdAndIsActive(taskId, workspaceId, true).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId).orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        // Validate target status
        if (request.getTargetStatus() == null) {
            throw new BusinessException(ErrorCode.INVALID_TASK_STATUS);
        }

        // Calculate new position
        Double newPosition;
        if (request.getTargetPosition() != null) {
            // Use provided position
            newPosition = request.getTargetPosition();
        } else if (request.getTargetIndex() != null) {
            // Calculate position based on index
            newPosition = calculatePositionAtIndex(workspaceId, request.getTargetStatus(), request.getTargetIndex());
        } else {
            // Move to end of column
            newPosition = calculateNewPosition(workspaceId, request.getTargetStatus());
        }

        TaskStatus oldStatus = task.getStatus();
        Double oldPosition = task.getPosition();

        // Update task
        task.setStatus(request.getTargetStatus());
        task.setPosition(newPosition);
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        User movedByUser = userRepository.findById(userId).orElse(null);
        String movedByName = movedByUser != null ? movedByUser.getName() : "Unknown";

        // Send WebSocket notification for task move
        TaskUpdateEvent event = TaskUpdateEvent.taskMoved(
                workspaceId,
                savedTask.getProjectId(),
                savedTask.getId(),
                savedTask.getName(),
                oldStatus,
                request.getTargetStatus(),
                oldPosition,
                newPosition,
                userId,
                movedByName
        );

        webSocketService.sendTaskUpdateToWorkspace(workspaceId, event);
        if (savedTask.getProjectId() != null) {
            webSocketService.sendTaskUpdateToProject(savedTask.getProjectId(), event);
        }
        webSocketService.sendKanbanUpdate(workspaceId, event);
        webSocketService.sendActivityUpdate(workspaceId, event);

        return taskMapper.toTaskResponse(savedTask);
    }

    // ========== VIEW-SPECIFIC QUERIES ==========

    /**
     * Get tasks for Kanban view (grouped by status, ordered by position)
     */
    public List<TaskResponse> getTasksForKanbanView(String workspaceId, String userId) {
        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId).orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        List<Task> tasks = taskRepository.findByWorkspaceIdAndIsActiveOrderByPositionAsc(workspaceId, true);

        return tasks.stream().map(taskMapper::toTaskResponse).collect(Collectors.toList());
    }

    /**
     * Get tasks for Calendar view (filtered by date range)
     */
    public List<TaskResponse> getTasksForCalendarView(String workspaceId, LocalDateTime startDate, LocalDateTime endDate, String userId) {
        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId).orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        List<Task> tasks = taskRepository.findByWorkspaceIdAndDueAtBetweenAndIsActive(workspaceId, startDate, endDate, true);

        return tasks.stream().map(taskMapper::toTaskResponse).collect(Collectors.toList());
    }

    /**
     * Get tasks by status for Kanban columns
     */
    public List<TaskResponse> getTasksByStatus(String workspaceId, TaskStatus status, String userId) {
        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId).orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        List<Task> tasks = taskRepository.findByWorkspaceIdAndStatusAndIsActiveOrderByPositionAsc(workspaceId, status, true);

        return tasks.stream().map(taskMapper::toTaskResponse).collect(Collectors.toList());
    }

    // ========== VALIDATION HELPERS ==========

    /**
     * Validate task status
     */
    private void validateTaskStatus(TaskStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_TASK_STATUS);
        }
        // Additional business rules can be added here
    }

    /**
     * Update task status with position recalculation if needed
     */
    private void updateTaskStatusAndPosition(Task task, TaskStatus newStatus) {
        TaskStatus oldStatus = task.getStatus();

        if (oldStatus != newStatus) {
            // Status changed, recalculate position
            Double newPosition = calculateNewPosition(task.getWorkspaceId(), newStatus);
            task.setStatus(newStatus);
            task.setPosition(newPosition);
        }
    }
}