package com.jira.jira.mapper;
import com.jira.jira.dto.response.ProjectMemberResponse;
import com.jira.jira.dto.response.ProjectPermissions;
import com.jira.jira.dto.response.ProjectResponse;
import com.jira.jira.model.Project;
import com.jira.jira.model.User;
import com.jira.jira.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ProjectMapper {

    @Autowired
    protected UserRepository userRepository;

    @Mapping(target = "memberCount", expression = "java(project.getMemberIds().size())")
    @Mapping(target = "taskCount", expression = "java(0)") // TODO: Implement when Task entity is ready
    @Mapping(target = "completedTaskCount", expression = "java(0)") // TODO: Implement when Task entity is ready
    @Mapping(target = "progressPercentage", expression = "java(0.0)") // TODO: Calculate based on completed tasks
    @Mapping(target = "userRole", expression = "java(\"MEMBER\")") // TODO: Calculate based on current user
    @Mapping(target = "members", source = "project", qualifiedByName = "mapMembers")
    @Mapping(target = "permissions", source = "project", qualifiedByName = "mapPermissions")
    public abstract ProjectResponse toProjectResponse(Project project);

    @Named("mapMembers")
    protected List<ProjectMemberResponse> mapMembers(Project project) {
        return project.getMemberIds().stream()
                .map(memberId -> {
                    User user = userRepository.findById(memberId).orElse(null);
                    if (user == null) return null;

                    return ProjectMemberResponse.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .displayName(user.getName())
                            .role(project.getOwnerId().equals(memberId) ? "OWNER" : "MEMBER")
                            .joinedAt(project.getCreatedAt()) // TODO: Track actual join date
                            .isActive(user.getIsActive())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Named("mapPermissions")
    protected ProjectPermissions mapPermissions(Project project) {
        // TODO: Calculate permissions based on current user
        // For now, return default permissions for owner
        return ProjectPermissions.builder()
                .canEdit(true)
                .canDelete(true)
                .canAddMembers(true)
                .canRemoveMembers(true)
                .canCreateTasks(true)
                .canManageColumns(true)
                .canViewAnalytics(true)
                .canManageSettings(true)
                .build();
    }
}