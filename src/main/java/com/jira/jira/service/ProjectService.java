package com.jira.jira.service;

import com.jira.jira.dto.common.PaginationRequest;
import com.jira.jira.dto.common.PaginationResponse;
import com.jira.jira.dto.common.SearchFilterRequest;
import com.jira.jira.dto.request.AddProjectMemberRequest;
import com.jira.jira.dto.request.CreateProjectRequest;
import com.jira.jira.dto.request.UpdateProjectRequest;
import com.jira.jira.dto.response.ProjectResponse;
import com.jira.jira.dto.response.ProjectStatsResponse;
import com.jira.jira.exception.BusinessException;
import com.jira.jira.exception.ErrorCode;
import com.jira.jira.mapper.ProjectMapper;
import com.jira.jira.model.Project;
import com.jira.jira.model.User;
import com.jira.jira.model.Workspace;
import com.jira.jira.repository.ProjectRepository;
import com.jira.jira.repository.UserRepository;
import com.jira.jira.repository.WorkspaceRepository;
import com.jira.jira.util.PaginationUtils;
import com.jira.jira.util.SearchFilterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService extends BaseService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectMapper projectMapper;

    /**
     * Create new project in workspace
     */
    public ProjectResponse createProject(CreateProjectRequest request, String workspaceId, String ownerId) {
        // Validate input parameters
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Workspace ID is required");
        }
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Owner ID is required");
        }

        // Validate workspace exists and user has access
        Workspace workspace = workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        // Validate owner exists
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Validate project key format (alphanumeric, 2-10 characters)
        String projectKey = request.getKey().toUpperCase().trim();
        if (!projectKey.matches("^[A-Z0-9]{2,10}$")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Project key must be 2-10 alphanumeric characters");
        }

        // Check if project key already exists in workspace
        if (projectRepository.findByKeyAndWorkspaceIdAndIsActive(projectKey, workspaceId, true).isPresent()) {
            throw new BusinessException(ErrorCode.PROJECT_ALREADY_EXISTS, "Project key '" + projectKey + "' already exists in this workspace");
        }

        // Check if project name already exists in workspace
        if (projectRepository.findByNameAndWorkspaceIdAndIsActive(request.getName().trim(), workspaceId, true).isPresent()) {
            throw new BusinessException(ErrorCode.PROJECT_ALREADY_EXISTS, "Project name '" + request.getName() + "' already exists in this workspace");
        }

        // Create project
        Project project = new Project();
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        project.setKey(projectKey); // Store key in uppercase
        project.setProjectType(request.getProjectType() != null ? request.getProjectType() : "SOFTWARE");
        project.setWorkspaceId(workspaceId);
        project.setOwnerId(ownerId);
        project.setMemberIds(List.of(ownerId)); // Owner is automatically a member
        project.setPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        project.setAvatarUrl(request.getAvatarUrl());
        project.setActive(true);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(savedProject);
    }

    /**
     * Get projects in workspace with advanced filtering and pagination
     */
    @Transactional(readOnly = true)
    public PaginationResponse<ProjectResponse> getWorkspaceProjects(String workspaceId, String userId, SearchFilterRequest request) {
        // Validate workspace access
        workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        List<Project> projects = projectRepository.findByWorkspaceIdAndIsActive(workspaceId, true);

        // Define search fields
        List<Function<Project, String>> searchFields = List.of(
                Project::getName,
                Project::getDescription,
                Project::getKey
        );

        // Define custom filters
        List<Predicate<Project>> customFilters = List.of(
                // Project type filter
                project -> {
                    String projectType = request.getFilterAsString("project_type");
                    return projectType == null || project.getProjectType().equalsIgnoreCase(projectType);
                },
                // Public filter
                project -> {
                    Boolean isPublic = request.getFilterAsBoolean("is_public");
                    return isPublic == null || project.isPublic() == isPublic;
                },
                // Owner filter
                project -> {
                    String ownerId = request.getFilterAsString("owner_id");
                    return ownerId == null || project.getOwnerId().equals(ownerId);
                }
        );

        // Apply search filter
        if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
            projects = SearchFilterUtils.applyMultiFieldSearch(projects.stream(), request, searchFields)
                    .collect(Collectors.toList());
        }

        // Apply custom filters
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            for (Predicate<Project> filter : customFilters) {
                projects = projects.stream().filter(filter).collect(Collectors.toList());
            }
        }

        // Apply sorting
        PaginationRequest pagination = request.getPagination();
        if (pagination != null && pagination.getSortBy() != null && !pagination.getSortBy().trim().isEmpty()) {
            projects = PaginationUtils.sortList(projects, pagination, project -> getProjectSortKey(project, pagination));
        }

        // Apply pagination
        List<Project> paginatedProjects = PaginationUtils.paginateList(projects, pagination);

        // Create pagination response
        PaginationResponse<Project> paginationResponse = PaginationResponse.<Project>builder()
                .content(paginatedProjects)
                .page(pagination.getPage())
                .size(pagination.getSize())
                .totalElements((long) projects.size())
                .totalPages(PaginationUtils.calculateTotalPages(projects.size(), pagination.getSize()))
                .first(pagination.getPage() == 0)
                .last(pagination.getPage() >= PaginationUtils.calculateTotalPages(projects.size(), pagination.getSize()) - 1)
                .numberOfElements(paginatedProjects.size())
                .empty(paginatedProjects.isEmpty())
                .sortBy(pagination.getSortBy())
                .sortDirection(String.valueOf(pagination.getSortDirection()))
                .build();

        // Convert to response DTOs
        List<ProjectResponse> projectResponses = paginatedProjects.stream()
                .map(projectMapper::toProjectResponse)
                .collect(Collectors.toList());

        return PaginationResponse.<ProjectResponse>builder()
                .content(projectResponses)
                .page(paginationResponse.getPage())
                .size(paginationResponse.getSize())
                .totalElements(paginationResponse.getTotalElements())
                .totalPages(paginationResponse.getTotalPages())
                .first(paginationResponse.getFirst())
                .last(paginationResponse.getLast())
                .numberOfElements(paginationResponse.getNumberOfElements())
                .empty(paginationResponse.getEmpty())
                .sortBy(paginationResponse.getSortBy())
                .sortDirection(paginationResponse.getSortDirection())
                .build();
    }

    /**
     * Get sort key for project sorting
     */
    private Comparable getProjectSortKey(Project project, PaginationRequest pagination) {
        if (pagination == null || pagination.getSortBy() == null) {
            return project.getName();
        }

        String sortBy = pagination.getSortBy().toLowerCase();
        switch (sortBy) {
            case "name":
                return project.getName();
            case "key":
                return project.getKey();
            case "project_type":
                return project.getProjectType();
            case "created_at":
                return project.getCreatedAt();
            case "updated_at":
                return project.getUpdatedAt();
            case "owner_id":
                return project.getOwnerId();
            default:
                return project.getName();
        }
    }

    /**
     * Get projects in workspace (backward compatibility)
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getWorkspaceProjects(String workspaceId, String userId) {
        SearchFilterRequest request = SearchFilterRequest.builder()
                .pagination(PaginationRequest.builder().page(0).size(20).build())
                .build();

        PaginationResponse<ProjectResponse> response = getWorkspaceProjects(workspaceId, userId, request);
        return response.getContent();
    }

    /**
     * Get user's projects across all workspaces
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(String userId) {
        List<Project> projects = projectRepository.findByUserIdAndIsActive(userId, true);
        return projects.stream()
                .map(projectMapper::toProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get project by ID (with permission check)
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(String projectId, String userId) {
        Project project = projectRepository.findByIdAndUserIdAndIsActive(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        return projectMapper.toProjectResponse(project);
    }

    /**
     * Update project
     */
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request, String userId) {
        Project project = projectRepository.findByIdAndOwnerIdAndIsActive(projectId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));

        // Check name uniqueness if name is being updated
        if (request.getName() != null && !request.getName().equals(project.getName())) {
            if (projectRepository.findByNameAndWorkspaceIdAndIsActive(request.getName(), project.getWorkspaceId(), true).isPresent()) {
                throw new BusinessException(ErrorCode.PROJECT_ALREADY_EXISTS);
            }
            project.setName(request.getName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getProjectType() != null) {
            project.setProjectType(request.getProjectType());
        }

        if (request.getIsPublic() != null) {
            project.setPublic(request.getIsPublic());
        }

        if (request.getAvatarUrl() != null) {
            project.setAvatarUrl(request.getAvatarUrl());
        }

        project.setUpdatedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(savedProject);
    }

    /**
     * Delete project (soft delete)
     */
    public void deleteProject(String projectId, String userId) {
        Project project = projectRepository.findByIdAndOwnerIdAndIsActive(projectId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));

        project.setActive(false);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    /**
     * Add member to project
     */
    public ProjectResponse addMember(String projectId, AddProjectMemberRequest request, String userId) {
        // Check if user has permission to add members (owner only)
        Project project = projectRepository.findByIdAndOwnerIdAndIsActive(projectId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));

        // Find user to add
        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Check if user is already a member
        if (project.getMemberIds().contains(userToAdd.getId())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // Add user to members list
        List<String> memberIds = project.getMemberIds();
        memberIds.add(userToAdd.getId());
        project.setMemberIds(memberIds);
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(savedProject);
    }

    /**
     * Remove member from project
     */
    public ProjectResponse removeMember(String projectId, String memberId, String userId) {
        // Check if user has permission to remove members (owner only)
        Project project = projectRepository.findByIdAndOwnerIdAndIsActive(projectId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));

        // Cannot remove owner
        if (project.getOwnerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        // Remove user from members list
        List<String> memberIds = project.getMemberIds();
        memberIds.remove(memberId);
        project.setMemberIds(memberIds);
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(savedProject);
    }

    /**
     * Get public projects in workspace
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getPublicProjects(String workspaceId) {
        List<Project> projects = projectRepository.findByWorkspaceIdAndIsPublicAndIsActive(workspaceId, true, true);
        return projects.stream()
                .map(projectMapper::toProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get project statistics
     */
    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(String projectId, String userId) {
        // Validate project access
        Project project = projectRepository.findByIdAndUserIdAndIsActive(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // TODO: Implement actual task statistics when Task entity is ready
        // For now, return mock data
        return ProjectStatsResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(0) // TODO: Count from Task entity
                .completedTasks(0) // TODO: Count completed tasks
                .inProgressTasks(0) // TODO: Count in-progress tasks
                .todoTasks(0) // TODO: Count todo tasks
                .progressPercentage(0.0) // TODO: Calculate based on completed tasks
                .totalMembers(project.getMemberIds().size())
                .activeMembers(project.getMemberIds().size()) // TODO: Count active members
                .createdAt(project.getCreatedAt())
                .lastActivity(project.getUpdatedAt()) // TODO: Get last task activity
                .taskCompletionRate(0.0) // TODO: Calculate completion rate
                .averageTaskCompletionTime(0.0) // TODO: Calculate average completion time
                .build();
    }
}

