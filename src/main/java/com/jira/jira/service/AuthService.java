package com.jira.jira.service;

import com.jira.jira.dto.request.LoginRequest;
import com.jira.jira.dto.request.RegisterRequest;
import com.jira.jira.dto.response.LoginResponse;
import com.jira.jira.dto.response.RegisterResponse;
import com.jira.jira.dto.response.TokenValidationResponse;
import com.jira.jira.dto.response.UserInfoResponse;
import com.jira.jira.exception.BusinessException;
import com.jira.jira.exception.ErrorCode;
import com.jira.jira.mapper.UserMapper;
import com.jira.jira.model.User;
import com.jira.jira.repository.UserRepository;
import com.jira.jira.security.JwtUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    UserRepository userRepository;
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
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Map to response using mapper
        return userMapper.toRegisterResponse(savedUser, token);
    }

    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Map to response using mapper
        return userMapper.toLoginResponse(user, token);
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
}
