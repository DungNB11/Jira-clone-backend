package com.jira.jira.repository;

import com.jira.jira.model.Task;
import com.jira.jira.model.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    // ========== POSITION-BASED QUERIES FOR KANBAN ==========

    // Find tasks by workspace ordered by position (for Kanban view)
    List<Task> findByWorkspaceIdAndIsActiveOrderByPositionAsc(String workspaceId, boolean isActive);

    // Find tasks by workspace and status ordered by position (for Kanban columns)
    List<Task> findByWorkspaceIdAndStatusAndIsActiveOrderByPositionAsc(String workspaceId, TaskStatus status, boolean isActive);

    // Find tasks by project and status ordered by position
    List<Task> findByProjectIdAndStatusAndIsActiveOrderByPositionAsc(String projectId, TaskStatus status, boolean isActive);

    // Get max position in a workspace/status combination
    Optional<Task> findFirstByWorkspaceIdAndStatusAndIsActiveOrderByPositionDesc(String workspaceId, TaskStatus status, boolean isActive);

    // Get max position in a project/status combination
    Optional<Task> findFirstByProjectIdAndStatusAndIsActiveOrderByPositionDesc(String projectId, TaskStatus status, boolean isActive);

    // ========== TABLE VIEW QUERIES ==========

    // Find tasks by workspace (for Table view with custom sorting)
    List<Task> findByWorkspaceIdAndIsActive(String workspaceId, boolean isActive);

    // Find tasks by project (for Table view)
    List<Task> findByProjectIdAndIsActive(String projectId, boolean isActive);

    // ========== CALENDAR VIEW QUERIES ==========

    // Find tasks due in date range (for Calendar view)
    @Query("{ 'workspaceId': ?0, 'dueAt': { $gte: ?1, $lte: ?2 }, 'isActive': ?3 }")
    List<Task> findByWorkspaceIdAndDueAtBetweenAndIsActive(
            String workspaceId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            boolean isActive
    );

    // Find tasks created in date range
    @Query("{ 'workspaceId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 }, 'isActive': ?3 }")
    List<Task> findByWorkspaceIdAndCreatedAtBetweenAndIsActive(
            String workspaceId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            boolean isActive
    );

    // ========== ASSIGNMENT & USER QUERIES ==========

    // Find tasks by assignee
    List<Task> findByAssigneeIdAndIsActive(String assigneeId, boolean isActive);

    // Find tasks by creator
    List<Task> findByCreatedByAndIsActive(String createdBy, boolean isActive);

    // ========== VALIDATION & ACCESS CONTROL ==========

    // Find task by ID with workspace validation
    @Query("{ '_id': ?0, 'workspaceId': ?1, 'isActive': ?2 }")
    Optional<Task> findByIdAndWorkspaceIdAndIsActive(String id, String workspaceId, boolean isActive);

    // Find task by ID with project validation
    @Query("{ '_id': ?0, 'projectId': ?1, 'isActive': ?2 }")
    Optional<Task> findByIdAndProjectIdAndIsActive(String id, String projectId, boolean isActive);

    // Find task by ID and active status
    Optional<Task> findByIdAndIsActive(String id, boolean isActive);

    // User access validation - user can access if they are assignee, creator, or workspace member
    @Query("{'_id': ?0, '$or': [" +
            "{'assigneeId': ?1}, " +
            "{'createdBy': ?1}" +
            "], 'isActive': true}")
    Optional<Task> findByIdAndUserIdAndIsActive(String taskId, String userId);

    // ========== OVERDUE & STATUS QUERIES ==========

    // Find overdue tasks (updated to use TaskStatus enum)
    @Query("{ 'workspaceId': ?0, 'dueAt': { $lt: ?1 }, 'status': { $nin: ?2 }, 'isActive': ?3 }")
    List<Task> findByWorkspaceIdAndDueAtBeforeAndStatusNotInAndIsActive(
            String workspaceId,
            LocalDateTime dateTime,
            List<TaskStatus> excludeStatuses,
            boolean isActive
    );

    // Find overdue tasks (simple version)
    @Query("{'dueAt': {'$lte': ?0}, 'status': {'$ne': ?1}, 'isActive': ?2}")
    List<Task> findOverdueTasks(LocalDateTime currentDate, TaskStatus doneStatus, boolean isActive);

    @Query("{'workspaceId': ?0, 'dueAt': {'$lte': ?1}, 'status': {'$ne': ?2}, 'isActive': ?3}")
    List<Task> findOverdueTasksByWorkspace(String workspaceId, LocalDateTime currentDate, TaskStatus doneStatus, boolean isActive);

    // ========== SEARCH QUERIES ==========

    @Query("{'$or': [" +
            "{'name': {'$regex': ?0, '$options': 'i'}}, " +
            "{'description': {'$regex': ?0, '$options': 'i'}}" +
            "], 'workspaceId': ?1, 'isActive': ?2}")
    List<Task> searchTasksInWorkspace(String searchTerm, String workspaceId, boolean isActive);

    @Query("{'$or': [" +
            "{'name': {'$regex': ?0, '$options': 'i'}}, " +
            "{'description': {'$regex': ?0, '$options': 'i'}}" +
            "], 'projectId': ?1, 'isActive': ?2}")
    List<Task> searchTasksInProject(String searchTerm, String projectId, boolean isActive);

    // ========== COUNTING QUERIES ==========

    // Count tasks by workspace
    long countByWorkspaceIdAndIsActive(String workspaceId, boolean isActive);

    // Count tasks by project
    long countByProjectIdAndIsActive(String projectId, boolean isActive);

    // Count tasks by status in workspace (updated to use TaskStatus enum)
    long countByWorkspaceIdAndStatusAndIsActive(String workspaceId, TaskStatus status, boolean isActive);

    // Count tasks by status in project (updated to use TaskStatus enum)
    long countByProjectIdAndStatusAndIsActive(String projectId, TaskStatus status, boolean isActive);

    // ========== ADVANCED FILTERING ==========

    // Find tasks by multiple criteria (updated to use TaskStatus enum)
    @Query("{ 'workspaceId': ?0, 'projectId': { $in: ?1 }, 'assigneeId': { $in: ?2 }, 'status': { $in: ?3 }, 'isActive': ?4 }")
    List<Task> findByWorkspaceIdAndProjectIdInAndAssigneeIdInAndStatusInAndIsActive(
            String workspaceId,
            List<String> projectIds,
            List<String> assigneeIds,
            List<TaskStatus> statuses,
            boolean isActive
    );

    // ========== POSITION MANAGEMENT HELPERS ==========

    // Check if position exists in workspace/status
    boolean existsByWorkspaceIdAndStatusAndPositionAndIsActive(String workspaceId, TaskStatus status, Double position, boolean isActive);

    // Check if position exists in project/status
    boolean existsByProjectIdAndStatusAndPositionAndIsActive(String projectId, TaskStatus status, Double position, boolean isActive);

    // Find tasks with null position (for potential cleanup)
    List<Task> findByPositionIsNullAndIsActive(boolean isActive);
}
