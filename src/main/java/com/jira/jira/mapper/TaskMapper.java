package com.jira.jira.mapper;

import com.jira.jira.dto.response.TaskResponse;
import com.jira.jira.model.Project;
import com.jira.jira.model.Task;
import com.jira.jira.model.User;
import com.jira.jira.model.Workspace;
import com.jira.jira.repository.ProjectRepository;
import com.jira.jira.repository.UserRepository;
import com.jira.jira.repository.WorkspaceRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected WorkspaceRepository workspaceRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Mapping(target = "assignee", source = "task", qualifiedByName = "mapAssignee")
    @Mapping(target = "createdUser", source = "task", qualifiedByName = "mapCreatedUser")
    @Mapping(target = "workspace", source = "task", qualifiedByName = "mapWorkspace")
    @Mapping(target = "project", source = "task", qualifiedByName = "mapProject")
    @Mapping(target = "isOverdue", expression = "java(task.isOverdue())")
    public abstract TaskResponse toTaskResponse(Task task);

    @Named("mapAssignee")
    protected TaskResponse.UserInfo mapAssignee(Task task) {
        if (task.getAssigneeId() == null) {
            return null;
        }

        User user = userRepository.findById(task.getAssigneeId()).orElse(null);
        if (user == null) {
            return null;
        }

        return TaskResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getName())
                .photoUrl(user.getPhotoUrl())
                .build();
    }

    @Named("mapCreatedUser")
    protected TaskResponse.UserInfo mapCreatedUser(Task task) {
        if (task.getCreatedBy() == null) {
            return null;
        }

        User user = userRepository.findById(task.getCreatedBy()).orElse(null);
        if (user == null) {
            return null;
        }

        return TaskResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getName())
                .photoUrl(user.getPhotoUrl())
                .build();
    }

    @Named("mapWorkspace")
    protected TaskResponse.WorkspaceInfo mapWorkspace(Task task) {
        if (task.getWorkspaceId() == null) {
            return null;
        }

        Workspace workspace = workspaceRepository.findById(task.getWorkspaceId()).orElse(null);
        if (workspace == null) {
            return null;
        }

        return TaskResponse.WorkspaceInfo.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .build();
    }

    @Named("mapProject")
    protected TaskResponse.ProjectInfo mapProject(Task task) {
        if (task.getProjectId() == null) {
            return null;
        }

        Project project = projectRepository.findById(task.getProjectId()).orElse(null);
        if (project == null) {
            return null;
        }

        return TaskResponse.ProjectInfo.builder()
                .id(project.getId())
                .name(project.getName())
                .key(project.getKey())
                .build();
    }

    public abstract List<TaskResponse> toTaskResponseList(List<Task> tasks);
}