package com.example.minitrello.service;

import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.security.UserSecurity;
import com.example.minitrello.service.UserServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserService implementation.
 * Tests the business logic of user management operations.
 *
 * Note: For unit tests, we're bypassing the @PreAuthorize security checks
 * as we're testing the service implementation directly.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserSecurity userSecurity;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user for reuse in tests
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
        lenient().when(userSecurity.isCurrentUser(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("Should create a new user successfully")
    void shouldCreateUserSuccessfully() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User createdUser = userService.createUser(testUser);

        // Assert
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(testUser.getId());
        assertThat(createdUser.getEmail()).isEqualTo(testUser.getEmail());

        // Verify that the password was encoded and the user was saved
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update an existing user successfully")
    void shouldUpdateUserSuccessfully() {
        // Arrange
        User updatedDetails = User.builder()
                .name("Updated Name")
                .password("newPassword")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User updatedUser = userService.updateUser(1L, updatedDetails);

        // Assert
        assertThat(updatedUser).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        User updatedDetails = User.builder()
                .name("Updated Name")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(999L, updatedDetails))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void shouldFindUserByIdSuccessfully() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findById(1L);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(1L);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty Optional when user ID not found")
    void shouldReturnEmptyOptionalWhenUserIdNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<User> foundUser = userService.findById(999L);

        // Assert
        assertThat(foundUser).isEmpty();
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void shouldFindUserByEmailSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findByEmail("test@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should find all users with pagination")
    void shouldFindAllUsersWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> userList = List.of(testUser);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<User> foundUsers = userService.findAllUsers(pageable);

        // Assert
        assertThat(foundUsers).isNotNull();
        assertThat(foundUsers.getContent()).hasSize(1);
        assertThat(foundUsers.getContent().get(0).getId()).isEqualTo(testUser.getId());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        // Act
        boolean result = userService.deleteUser(1L);

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent user")
    void shouldReturnFalseWhenDeletingNonExistentUser() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        boolean result = userService.deleteUser(999L);

        // Assert
        assertThat(result).isFalse();
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act
        boolean exists = userService.existsByEmail("test@example.com");

        // Assert
        assertThat(exists).isTrue();
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }
}