package com.example.minitrello.service;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AdminService implementation.
 * Tests the business logic of admin operations.
 */
@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User regularUser;
    private User adminUser;
    private UserDto regularUserDto;
    private UserDto adminUserDto;

    @BeforeEach
    void setUp() {
        // Create a regular test user
        regularUser = User.builder()
                .id(1L)
                .name("Regular User")
                .email("user@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        regularUserDto = UserDto.builder()
                .id(1L)
                .name("Regular User")
                .email("user@example.com")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        // Create an admin test user
        adminUser = User.builder()
                .id(2L)
                .name("Admin User")
                .email("admin@example.com")
                .password("password")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        adminUserDto = UserDto.builder()
                .id(2L)
                .name("Admin User")
                .email("admin@example.com")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should change user role successfully")
    void shouldChangeUserRoleSuccessfully() {
        // Arrange
        User updatedUser = User.builder()
                .id(1L)
                .name("Regular User")
                .email("user@example.com")
                .password("password")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        UserDto updatedUserDto = UserDto.builder()
                .id(1L)
                .name("Regular User")
                .email("user@example.com")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userService.toDto(updatedUser)).thenReturn(updatedUserDto);

        // Act
        UserDto result = adminService.changeUserRole(1L, Role.ROLE_ADMIN);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userService, times(1)).toDto(updatedUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when changing role of non-existent user")
    void shouldThrowResourceNotFoundExceptionWhenChangingRoleOfNonExistentUser() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.changeUserRole(999L, Role.ROLE_ADMIN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id : '999'");

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get all users with details")
    void shouldGetAllUsersWithDetails() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(regularUser, adminUser);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        Page<UserDto> dtoPage = new PageImpl<>(List.of(regularUserDto, adminUserDto), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userService.toDto(regularUser)).thenReturn(regularUserDto);
        when(userService.toDto(adminUser)).thenReturn(adminUserDto);

        // Act
        Page<UserDto> result = adminService.getAllUsersDetailed(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(result.getContent().get(1).getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should disable user successfully")
    void shouldDisableUserSuccessfully() {
        // Arrange
        User disabledUser = User.builder()
                .id(1L)
                .name("Regular User")
                .email("user@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();

        UserDto disabledUserDto = UserDto.builder()
                .id(1L)
                .name("Regular User")
                .email("user@example.com")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(disabledUser);
        when(userService.toDto(disabledUser)).thenReturn(disabledUserDto);

        // Act
        UserDto result = adminService.disableUser(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isFalse();
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userService, times(1)).toDto(disabledUser);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when trying to disable admin user")
    void shouldThrowAccessDeniedExceptionWhenTryingToDisableAdminUser() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThatThrownBy(() -> adminService.disableUser(2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Cannot disable an admin account");

        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should enable user successfully")
    void shouldEnableUserSuccessfully() {
        // Arrange
        // Create a disabled user
        User disabledUser = User.builder()
                .id(3L)
                .name("Disabled User")
                .email("disabled@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .isActive(false)
                .build();

        User enabledUser = User.builder()
                .id(3L)
                .name("Disabled User")
                .email("disabled@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        UserDto enabledUserDto = UserDto.builder()
                .id(3L)
                .name("Disabled User")
                .email("disabled@example.com")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(disabledUser));
        when(userRepository.save(any(User.class))).thenReturn(enabledUser);
        when(userService.toDto(enabledUser)).thenReturn(enabledUserDto);

        // Act
        UserDto result = adminService.enableUser(3L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isTrue();
        verify(userRepository, times(1)).findById(3L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userService, times(1)).toDto(enabledUser);
    }
}