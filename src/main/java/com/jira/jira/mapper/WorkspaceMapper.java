package com.jira.jira.mapper;

import com.jira.jira.dto.response.WorkspaceMemberResponse;
import com.jira.jira.dto.response.WorkspacePermissions;
import com.jira.jira.dto.response.WorkspaceResponse;
import com.jira.jira.model.User;
import com.jira.jira.model.Workspace;
import com.jira.jira.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class WorkspaceMapper {

    @Autowired
    protected UserRepository userRepository;

    @Mapping(target = "memberCount", expression = "java(workspace.getMemberIds().size())")
    @Mapping(target = "projectCount", expression = "java(0)") // TODO: Implement when Project entity is ready
    @Mapping(target = "taskCount", expression = "java(0)") // TODO: Implement when Task entity is ready
    @Mapping(target = "activeMemberCount", expression = "java(workspace.getMemberIds().size())") // TODO: Count active members
    @Mapping(target = "userRole", expression = "java(\"MEMBER\")") // TODO: Calculate based on current user
    @Mapping(target = "members", source = "workspace", qualifiedByName = "mapMembers")
    @Mapping(target = "permissions", source = "workspace", qualifiedByName = "mapPermissions")
    public abstract WorkspaceResponse toWorkspaceResponse(Workspace workspace);

    @Named("mapMembers")
    protected List<WorkspaceMemberResponse> mapMembers(Workspace workspace) {
        return workspace.getMemberIds().stream()
                .map(memberId -> {
                    User user = userRepository.findById(memberId).orElse(null);
                    if (user == null) return null;

                    return WorkspaceMemberResponse.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .role(workspace.getOwnerId().equals(memberId) ? "OWNER" : "MEMBER")
                            .joinedAt(workspace.getCreatedAt())
                            .isActive(user.getIsActive())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Named("mapPermissions")
    protected WorkspacePermissions mapPermissions(Workspace workspace) {
        // TODO: Calculate permissions based on current user
        // For now, return default permissions for owner
        return WorkspacePermissions.builder()
                .canEdit(true)
                .canDelete(true)
                .canAddMembers(true)
                .canRemoveMembers(true)
                .canCreateProjects(true)
                .canManageSettings(true)
                .canViewAnalytics(true)
                .build();
    }
}