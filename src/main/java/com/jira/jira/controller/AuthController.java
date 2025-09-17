package com.jira.jira.controller;

import com.jira.jira.dto.request.LoginRequest;
import com.jira.jira.dto.request.RegisterRequest;
import com.jira.jira.dto.response.ApiResponse;
import com.jira.jira.dto.response.LoginResponse;
import com.jira.jira.dto.response.RegisterResponse;
import com.jira.jira.dto.response.TokenValidationResponse;
import com.jira.jira.service.AuthService;
import com.jira.jira.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response =  authService.register(request);
        return ResponseHelper.created(response, "User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseHelper.ok(response, "Login successful");
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@RequestHeader("Authorization") String token) {
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseHelper.ok(response, "Token is valid");
    }
}
