package com.example.minitrello.mapper;

import com.example.minitrello.dto.auth.LoginResponse;
import com.example.minitrello.dto.auth.RegisterRequest;
import com.example.minitrello.dto.user.SimpleUserDto;
import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.dto.user.UserUpdateDto;
import com.example.minitrello.model.User;
import com.example.minitrello.security.UserDetailsImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    SimpleUserDto toSimpleUserDto(User user);

    UserDto toDto(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "assignedTasks", ignore = true)
    @Mapping(target = "role", constant = "ROLE_USER")
    @Mapping(target = "isActive", constant = "true")
    User toEntity(RegisterRequest registerRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "assignedTasks", ignore = true)
    void updateUserFromDto(UserUpdateDto updateDto, @MappingTarget User user);

    @Mapping(target = "tokenType", constant = "Bearer")
    LoginResponse toLoginResponse(UserDetailsImpl userDetails, String token);
}