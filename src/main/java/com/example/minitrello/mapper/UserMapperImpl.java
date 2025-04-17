package com.example.minitrello.mapper;

import com.example.minitrello.dto.auth.LoginResponse;
import com.example.minitrello.dto.auth.RegisterRequest;
import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.dto.user.UserUpdateDto;
import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.security.UserDetailsImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Manual implementation of UserMapper.
 * Maps between User entities and DTOs.
 * Note: In a production app, consider using MapStruct for automatic mapping.
 */
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(user.getIsActive())
                .assignedTaskCount(user.getAssignedTasks() != null ? user.getAssignedTasks().size() : 0)
                .build();
    }

    @Override
    public User toEntity(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return null;
        }

        return User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword()) // Will be encoded in service
                .build();
    }

    @Override
    public void updateUserFromDto(UserUpdateDto updateDto, User user) {
        if (updateDto == null || user == null) {
            return;
        }

        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }
    }

    @Override
    public LoginResponse toLoginResponse(UserDetailsImpl userDetails, String token) {
        if (userDetails == null) {
            return null;
        }

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(userDetails.getId())
                .name(userDetails.getName())
                .email(userDetails.getEmail())
                .role(Role.valueOf(userDetails.getAuthorities().iterator().next().getAuthority()))
                .build();
    }
}