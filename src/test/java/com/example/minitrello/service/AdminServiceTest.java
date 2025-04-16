package com.example.minitrello.service;

import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.AdminServiceImpl;
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

    @InjectMocks
    private AdminServiceImpl adminService;

    private User regularUser;
    private User adminUser;

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

        // Create an admin test user
        adminUser = User.builder()
                .id(2L)
                .name("Admin User")
                .email("admin@example.com")
                .password("password")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should change user role successfully")
        // We're bypassing the @PreAuthorize check for unit tests
    void shouldChangeUserRoleSuccessfully() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        // Act
        User updatedUser = adminService.changeUserRole(1L, Role.ROLE_ADMIN);

        // Assert
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getRole()).isEqualTo(Role.ROLE_ADMIN);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when changing role of non-existent user")
    void shouldThrowResourceNotFoundExceptionWhenChangingRoleOfNonExistentUser() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.changeUserRole(999L, Role.ROLE_ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);

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

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<User> result = adminService.getAllUsersDetailed(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).contains(regularUser, adminUser);

        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should disable user successfully")
    void shouldDisableUserSuccessfully() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        // Act
        User disabledUser = adminService.disableUser(1L);

        // Assert
        assertThat(disabledUser).isNotNull();
        assertThat(disabledUser.getIsActive()).isFalse();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when trying to disable admin user")
    void shouldThrowAccessDeniedExceptionWhenTryingToDisableAdminUser() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(adminUser));

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

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(disabledUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        // Act
        User enabledUser = adminService.enableUser(3L);

        // Assert
        assertThat(enabledUser).isNotNull();
        assertThat(enabledUser.getIsActive()).isTrue();

        verify(userRepository, times(1)).findById(3L);
        verify(userRepository, times(1)).save(any(User.class));
    }
}