package com.jira.jira.repository;

import com.jira.jira.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    List<Project> findByWorkspaceIdAndIsActive(String workspaceId, boolean isActive);

    List<Project> findByOwnerIdAndIsActive(String ownerId, boolean isActive);

    @Query("{'$or': [{'ownerId': ?0}, {'memberIds': ?0}], 'isActive': ?1}")
    List<Project> findByUserIdAndIsActive(String userId, boolean isActive);

    Optional<Project> findByKeyAndWorkspaceIdAndIsActive(String key, String workspaceId, boolean isActive);

    Optional<Project> findByNameAndWorkspaceIdAndIsActive(String name, String workspaceId, boolean isActive);

    @Query("{'_id': ?0, '$or': [{'ownerId': ?1}, {'memberIds': ?1}], 'isActive': true}")
    Optional<Project> findByIdAndUserIdAndIsActive(String projectId, String userId);

    Optional<Project> findByIdAndOwnerIdAndIsActive(String projectId, String ownerId, boolean isActive);

    List<Project> findByWorkspaceIdAndIsPublicAndIsActive(String workspaceId, boolean isPublic, boolean isActive);

    long countByWorkspaceIdAndIsActive(String workspaceId, boolean isActive);
    Optional<Project> findByIdAndWorkspaceIdAndIsActive(String projectId, String workspaceId, boolean isActive);
    Optional<Project> findByIdAndIsActive(String projectId, boolean isActive);
}
