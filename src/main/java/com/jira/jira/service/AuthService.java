package com.jira.jira.service;

import com.jira.jira.dto.request.LoginRequest;
import com.jira.jira.dto.request.RefreshTokenRequest;
import com.jira.jira.dto.request.RegisterRequest;
import com.jira.jira.dto.response.*;
import com.jira.jira.exception.BusinessException;
import com.jira.jira.exception.ErrorCode;
import com.jira.jira.mapper.UserMapper;
import com.jira.jira.model.RefreshToken;
import com.jira.jira.model.User;
import com.jira.jira.repository.RefreshTokenRepository;
import com.jira.jira.repository.UserRepository;
import com.jira.jira.security.JwtUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    UserRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;
    UserMapper userMapper;
    AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String accessToken = jwtUtil.generateToken(savedUser.getEmail());
        String refreshToken = createRefreshToken(savedUser.getId());

        // Map to response using mapper
        RegisterResponse response = userMapper.toRegisterResponse(savedUser, accessToken);

        response.setRefreshToken(refreshToken);
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        // Generate JWT tokens
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user.getId());

        // Map to response using mapper
        LoginResponse response = userMapper.toLoginResponse(user, accessToken);

        response.setRefreshToken(refreshToken);

        return response;
    }

    public TokenValidationResponse validateToken(String token) {
        // Remove Bearer prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // Extract username from token
        String email = jwtUtil.extractUsername(token);

        // Get user details
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        // Map to response using mapper
        return userMapper.toTokenValidationResponse(user);
    }

    public UserInfoResponse getCurrentUser(String token) {
        TokenValidationResponse validation = validateToken(token);
        return validation.getUser();
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndIsActive(request.getRefreshToken(), true).orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Delete expired refresh token
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        // Get user
        User user = userRepository.findById(refreshToken.getUserId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        String newAccessToken = jwtUtil.generateToken(user.getEmail());

        String newRefreshToken = createRefreshToken(user.getId());

        refreshTokenRepository.delete(refreshToken);

        RefreshTokenResponse response = RefreshTokenResponse.builder().accessToken(newAccessToken).build();

        response.setRefreshToken(newRefreshToken);

        return response;
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndIsActive(refreshToken, true).ifPresent(refreshTokenRepository::delete);
    }

    public void logoutAllDevices(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }


    private String createRefreshToken(String userId) {
        // Generate unique refresh token
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        refreshTokenRepository.save(refreshToken);

        return refreshTokenValue;
    }
}
