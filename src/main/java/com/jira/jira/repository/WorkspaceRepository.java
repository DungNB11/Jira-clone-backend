package com.jira.jira.repository;

import com.jira.jira.model.Workspace;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends MongoRepository<Workspace, String> {
    Optional<Workspace> findByName(String name);

    List<Workspace> findByOwnerId(String ownerId);

    List<Workspace> findByMemberIdsContaining(String userId);

    List<Workspace> findByIsPublicTrue();

    Optional<Workspace> findByIdAndIsActiveTrue(String id);

    List<Workspace> findByOwnerIdAndIsActiveTrue(String ownerId);

    List<Workspace> findByMemberIdsContainingAndIsActiveTrue(String userId);

    List<Workspace> findByIsPublicTrueAndIsActiveTrue();

    List<Workspace> findByOwnerIdAndIsActive(String ownerId, boolean isActive);

    @Query("{'$or': [{'ownerId': ?0}, {'memberIds': ?0}], 'isActive': ?1}")
    List<Workspace> findByUserIdAndIsActive(String userId, boolean isActive);

    Optional<Workspace> findByNameAndOwnerIdAndIsActive(String name, String ownerId, boolean isActive);

    List<Workspace> findByIsPublicAndIsActive(boolean isPublic, boolean isActive);

    @Query("{'_id': ?0, '$or': [{'ownerId': ?1}, {'memberIds': ?1}], 'isActive': true}")
    Optional<Workspace> findByIdAndUserIdAndIsActive(String workspaceId, String userId);

    Optional<Workspace> findByIdAndOwnerIdAndIsActive(String workspaceId, String ownerId, boolean isActive);
}
