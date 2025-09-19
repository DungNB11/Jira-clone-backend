package com.jira.jira.controller;
import com.jira.jira.dto.common.PaginationRequest;
import com.jira.jira.dto.common.PaginationResponse;
import com.jira.jira.dto.common.SearchFilterRequest;
import com.jira.jira.dto.request.AddProjectMemberRequest;
import com.jira.jira.dto.request.CreateProjectRequest;
import com.jira.jira.dto.request.UpdateProjectRequest;
import com.jira.jira.dto.response.ApiResponse;
import com.jira.jira.dto.response.ProjectResponse;
import com.jira.jira.dto.response.ProjectStatsResponse;
import com.jira.jira.service.AuthService;
import com.jira.jira.service.ProjectService;
import com.jira.jira.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AuthService authService;

    /**
     * Create new project in workspace
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable String workspaceId,
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        ProjectResponse response = projectService.createProject(request, workspaceId, userId);
        return ResponseHelper.created(response, "Project created successfully");
    }

    /**
     * Get projects in workspace with advanced filtering and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<ProjectResponse>>> getWorkspaceProjects(
            @PathVariable String workspaceId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String project_type,
            @RequestParam(required = false) Boolean is_public,
            @RequestParam(required = false) String owner_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort_by,
            @RequestParam(required = false) String sort_direction,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();

        Map<String, Object> filters = new HashMap<>();
        if (project_type != null) filters.put("project_type", project_type);
        if (is_public != null) filters.put("is_public", is_public);
        if (owner_id != null) filters.put("owner_id", owner_id);

        // Build search filter request
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

        PaginationResponse<ProjectResponse> projects = projectService.getWorkspaceProjects(workspaceId, userId, request);
        return ResponseHelper.ok(projects, "Projects retrieved successfully");
    }

    /**
     * Get public projects in workspace
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getPublicProjects(
            @PathVariable String workspaceId) {

        List<ProjectResponse> projects = projectService.getPublicProjects(workspaceId);
        return ResponseHelper.ok(projects, "Public projects retrieved successfully");
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable String workspaceId,
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        ProjectResponse project = projectService.getProjectById(projectId, userId);
        return ResponseHelper.ok(project, "Project retrieved successfully");
    }

    /**
     * Update project
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable String workspaceId,
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        ProjectResponse response = projectService.updateProject(projectId, request, userId);
        return ResponseHelper.ok(response, "Project updated successfully");
    }

    /**
     * Delete project
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable String workspaceId,
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        projectService.deleteProject(projectId, userId);
        return ResponseHelper.ok(null, "Project deleted successfully");
    }

    /**
     * Add member to project
     */
    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<ProjectResponse>> addMember(
            @PathVariable String workspaceId,
            @PathVariable String projectId,
            @Valid @RequestBody AddProjectMemberRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        ProjectResponse response = projectService.addMember(projectId, request, userId);
        return ResponseHelper.ok(response, "Member added successfully");
    }

    /**
     * Remove member from project
     */
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> removeMember(
            @PathVariable String workspaceId,
            @PathVariable String projectId,
            @PathVariable String memberId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        ProjectResponse response = projectService.removeMember(projectId, memberId, userId);
        return ResponseHelper.ok(response, "Member removed successfully");
    }

    /**
     * Get project statistics
     */
    @GetMapping("/{projectId}/stats")
    public ResponseEntity<ApiResponse<ProjectStatsResponse>> getProjectStats(
            @PathVariable String workspaceId,
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        ProjectStatsResponse stats = projectService.getProjectStats(projectId, userId);
        return ResponseHelper.ok(stats, "Project statistics retrieved successfully");
    }
}

/**
 * Global Project Controller for cross-workspace operations
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
class GlobalProjectController {

    private final ProjectService projectService;
    private final AuthService authService;

    /**
     * Get user's projects across all workspaces
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getUserProjects(
            @RequestHeader("Authorization") String token) {

        String userId = authService.validateToken(token).getUser().getId();
        List<ProjectResponse> projects = projectService.getUserProjects(userId);
        return ResponseHelper.ok(projects, "User projects retrieved successfully");
    }
}

