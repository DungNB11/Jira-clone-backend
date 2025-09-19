package com.jira.jira.controller;

import com.jira.jira.dto.request.AddMemberRequest;
import com.jira.jira.dto.request.CreateWorkspaceRequest;
import com.jira.jira.dto.request.UpdateWorkspaceRequest;
import com.jira.jira.dto.response.ApiResponse;
import com.jira.jira.dto.response.WorkspaceResponse;
import com.jira.jira.service.AuthService;
import com.jira.jira.service.WorkspaceService;
import com.jira.jira.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.getCurrentUser(token).getId();
        WorkspaceResponse response = workspaceService.createWorkspace(request, userId);
        return ResponseHelper.created(response, "Workspace created successfully");
    }

    /**
     * Get user's workspaces
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getUserWorkspaces(
            @RequestHeader("Authorization") String token) {

        String userId = authService.getCurrentUser(token).getId();
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(userId);
        return ResponseHelper.ok(workspaces, "Workspaces retrieved successfully");
    }

    /**
     * Get public workspaces
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getPublicWorkspaces() {
        List<WorkspaceResponse> workspaces = workspaceService.getPublicWorkspaces();
        return ResponseHelper.ok(workspaces, "Public workspaces retrieved successfully");
    }

    /**
     * Get workspace by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        String userId = authService.getCurrentUser(token).getId();
        WorkspaceResponse workspace = workspaceService.getWorkspaceById(id, userId);
        return ResponseHelper.ok(workspace, "Workspace retrieved successfully");
    }

    /**
     * Update workspace
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable String id,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.getCurrentUser(token).getId();
        WorkspaceResponse response = workspaceService.updateWorkspace(id, request, userId);
        return ResponseHelper.ok(response, "Workspace updated successfully");
    }

    /**
     * Delete workspace
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {

        String userId = authService.getCurrentUser(token).getId();
        workspaceService.deleteWorkspace(id, userId);
        return ResponseHelper.ok(null, "Workspace deleted successfully");
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> addMember(
            @PathVariable String id,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("Authorization") String token) {

        String userId = authService.getCurrentUser(token).getId();
        WorkspaceResponse response = workspaceService.addMember(id, request, userId);
        return ResponseHelper.ok(response, "Member added successfully");
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> removeMember(
            @PathVariable String id,
            @PathVariable String memberId,
            @RequestHeader("Authorization") String token) {

        String userId = authService.getCurrentUser(token).getId();
        WorkspaceResponse response = workspaceService.removeMember(id, memberId, userId);
        return ResponseHelper.ok(response, "Member removed successfully");
    }
}
