package com.jira.jira.mapper;

import com.jira.jira.dto.request.RegisterRequest;
import com.jira.jira.dto.response.LoginResponse;
import com.jira.jira.dto.response.RegisterResponse;
import com.jira.jira.dto.response.TokenValidationResponse;
import com.jira.jira.dto.response.UserInfoResponse;
import com.jira.jira.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserInfoResponse toUserInfoResponse(User user);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toUser(RegisterRequest request);

    default LoginResponse toLoginResponse(User user, String token) {
        if (user == null) {
            return null;
        }

        return LoginResponse.builder()
                .token(token)
                .user(toUserInfoResponse(user))
                .build();
    }

    default RegisterResponse toRegisterResponse(User user, String token) {
        if (user == null) {
            return null;
        }

        return RegisterResponse.builder()
                .token(token)
                .user(toUserInfoResponse(user))
                .build();
    }

    default TokenValidationResponse toTokenValidationResponse(User user) {
        if (user == null) {
            return null;
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .user(toUserInfoResponse(user))
                .build();
    }
}
