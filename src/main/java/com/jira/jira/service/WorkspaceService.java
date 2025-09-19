package com.jira.jira.service;

import com.jira.jira.dto.request.AddMemberRequest;
import com.jira.jira.dto.request.CreateWorkspaceRequest;
import com.jira.jira.dto.request.UpdateWorkspaceRequest;
import com.jira.jira.dto.response.WorkspaceResponse;
import com.jira.jira.exception.BusinessException;
import com.jira.jira.exception.ErrorCode;
import com.jira.jira.mapper.WorkspaceMapper;
import com.jira.jira.model.User;
import com.jira.jira.model.Workspace;
import com.jira.jira.repository.UserRepository;
import com.jira.jira.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;

    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, String ownerId) {
        // Validate owner exists
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (workspaceRepository.findByNameAndOwnerIdAndIsActive(request.getName(), ownerId, true).isPresent()) {
            throw new BusinessException(ErrorCode.WORKSPACE_ALREADY_EXISTS);
        }

        // Create workspace
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        workspace.setOwnerId(ownerId);
        workspace.setMemberIds(List.of(ownerId)); // Owner is automatically a member
        workspace.setActive(true);
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        return workspaceMapper.toWorkspaceResponse(savedWorkspace);
    }

    /**
     * Get user's workspaces
     */
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces(String userId) {
        List<Workspace> workspaces = workspaceRepository.findByUserIdAndIsActive(userId, true);
        return workspaces.stream()
                .map(workspaceMapper::toWorkspaceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get workspace by ID (with permission check)
     */
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(String workspaceId, String userId) {
        Workspace workspace = workspaceRepository.findByIdAndUserIdAndIsActive(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        return workspaceMapper.toWorkspaceResponse(workspace);
    }

    public WorkspaceResponse updateWorkspace(String workspaceId, UpdateWorkspaceRequest request, String userId) {
        Workspace workspace = workspaceRepository.findByIdAndOwnerIdAndIsActive(workspaceId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        if (request.getName() != null && !request.getName().equals(workspace.getName())) {
            if (workspaceRepository.findByNameAndOwnerIdAndIsActive(request.getName(), userId, true).isPresent()) {
                throw new BusinessException(ErrorCode.WORKSPACE_ALREADY_EXISTS);
            }
            workspace.setName(request.getName());
        }

        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }

        if (request.getIsPublic() != null) {
            workspace.setPublic(request.getIsPublic());
        }

        if (request.getAvatarUrl() != null) {
            workspace.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getJoinUrl() != null) {
            workspace.setJoinUrl(request.getJoinUrl());
        }

        workspace.setUpdatedAt(LocalDateTime.now());
        Workspace savedWorkspace = workspaceRepository.save(workspace);
        return workspaceMapper.toWorkspaceResponse(savedWorkspace);
    }

    public void deleteWorkspace(String workspaceId, String userId) {
        Workspace workspace = workspaceRepository.findByIdAndOwnerIdAndIsActive(workspaceId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        workspace.setActive(false);
        workspace.setUpdatedAt(LocalDateTime.now());
        workspaceRepository.save(workspace);
    }

    public WorkspaceResponse addMember(String workspaceId, AddMemberRequest request, String userId) {
        Workspace workspace = workspaceRepository.findByIdAndOwnerIdAndIsActive(workspaceId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (workspace.getMemberIds().contains(userToAdd.getId())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        List<String> memberIds = workspace.getMemberIds();
        memberIds.add(userToAdd.getId());
        workspace.setMemberIds(memberIds);
        workspace.setUpdatedAt(LocalDateTime.now());

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        return workspaceMapper.toWorkspaceResponse(savedWorkspace);
    }

    public WorkspaceResponse removeMember(String workspaceId, String memberId, String userId) {
        Workspace workspace = workspaceRepository.findByIdAndOwnerIdAndIsActive(workspaceId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        if (workspace.getOwnerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        List<String> memberIds = workspace.getMemberIds();
        memberIds.remove(memberId);
        workspace.setMemberIds(memberIds);
        workspace.setUpdatedAt(LocalDateTime.now());

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        return workspaceMapper.toWorkspaceResponse(savedWorkspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getPublicWorkspaces() {
        List<Workspace> workspaces = workspaceRepository.findByIsPublicAndIsActive(true, true);
        return workspaces.stream()
                .map(workspaceMapper::toWorkspaceResponse)
                .collect(Collectors.toList());
    }
}
