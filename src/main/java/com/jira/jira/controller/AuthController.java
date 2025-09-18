package com.jira.jira.controller;

import com.jira.jira.dto.request.LoginRequest;
import com.jira.jira.dto.request.RefreshTokenRequest;
import com.jira.jira.dto.request.RegisterRequest;
import com.jira.jira.dto.response.*;
import com.jira.jira.exception.BusinessException;
import com.jira.jira.exception.ErrorCode;
import com.jira.jira.service.AuthService;
import com.jira.jira.util.CookieUtil;
import com.jira.jira.util.ResponseHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse httpResponse) {
        RegisterResponse response =  authService.register(request);

        cookieUtil.setRefreshTokenCookie(httpResponse, response.getRefreshToken());
        return ResponseHelper.created(response, "User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request);

        cookieUtil.setRefreshTokenCookie(httpResponse, response.getRefreshToken());
        return ResponseHelper.ok(response, "Login successful");
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@RequestHeader("Authorization") String token) {
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseHelper.ok(response, "Token is valid");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        // Get refresh token from cookie
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        RefreshTokenResponse response = authService.refreshToken(refreshRequest);

        cookieUtil.setRefreshTokenCookie(httpResponse, response.getRefreshToken());

        // Refresh token is automatically excluded from JSON response due to @JsonIgnore

        return ResponseHelper.ok(response, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        // Get refresh token from cookie
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // Clear refresh token cookie
        cookieUtil.clearRefreshTokenCookie(httpResponse);

        return ResponseHelper.ok(null, "Logged out successfully");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @RequestHeader("Authorization") String token,
            HttpServletResponse httpResponse) {
        TokenValidationResponse validation = authService.validateToken(token);
        authService.logoutAllDevices(validation.getUser().getId());

        // Clear refresh token cookie
        cookieUtil.clearRefreshTokenCookie(httpResponse);

        return ResponseHelper.ok(null, "Logged out from all devices successfully");
    }
}
