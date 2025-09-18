package com.jira.jira.repository;

import com.jira.jira.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndIsActive(String token, Boolean isActive);

    List<RefreshToken> findByUserIdAndIsActive(String userId, Boolean isActive);

    List<RefreshToken> findByExpiresAtBeforeAndIsActive(LocalDateTime expiresAt, Boolean isActive);

    void deleteByUserId(String userId);

    void deleteByToken(String token);
}
