package com.example.minitrello.service;

import com.example.minitrello.dto.auth.LoginRequest;
import com.example.minitrello.dto.auth.LoginResponse;
import com.example.minitrello.dto.auth.RegisterRequest;
import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.UserMapper;
import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.security.JwtUtils;
import com.example.minitrello.security.UserDetailsImpl;
import com.example.minitrello.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AuthService implementation.
 * Tests the business logic of authentication operations.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserDto userDto;
    private UserDetailsImpl userDetails;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Create a test user for reuse in tests
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        // Create user details for authentication
        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_USER.name())))
                .build();

        // Create registration request
        registerRequest = RegisterRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();

        // Create login request
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userMapper.toEntity(registerRequest)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userService.toDto(testUser)).thenReturn(userDto);

        // Act
        UserDto result = authService.registerUser(registerRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(result.getName()).isEqualTo(registerRequest.getName());

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userService, times(1)).toDto(testUser);
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void shouldThrowExceptionWhenRegisteringWithExistingEmail() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate user and return JWT token")
    void shouldAuthenticateUserAndReturnToken() {
        // Arrange
        String jwtToken = "test.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateToken(authentication)).thenReturn(jwtToken);

        // Act
        LoginResponse response = authService.authenticateUser(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(jwtToken);
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getName()).isEqualTo(testUser.getName());
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateToken(authentication);
    }

    @Test
    @DisplayName("Should validate JWT token")
    void shouldValidateJwtToken() {
        // Arrange
        String token = "valid.jwt.token";
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);

        // Act
        boolean isValid = authService.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
        verify(jwtUtils, times(1)).validateJwtToken(token);
    }

    @Test
    @DisplayName("Should get current authenticated user DTO")
    void shouldGetCurrentAuthenticatedUserDto() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.of(testUser));
        when(userService.toDto(testUser)).thenReturn(userDto);

        // Act
        UserDto result = authService.getCurrentAuthenticatedUserDto();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userDto.getId());
        assertThat(result.getEmail()).isEqualTo(userDto.getEmail());
        verify(userRepository, times(1)).findById(userDetails.getId());
        verify(userService, times(1)).toDto(testUser);
    }

    @Test
    @DisplayName("Should get current authenticated user ID")
    void shouldGetCurrentAuthenticatedUserId() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        Long userId = authService.getCurrentAuthenticatedUserId();

        // Assert
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when no authenticated user is found")
    void shouldThrowExceptionWhenNoAuthenticatedUserIsFound() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentAuthenticatedUserDto())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User not authenticated");
    }

    @Test
    @DisplayName("Should throw exception when authenticated user is not found in database")
    void shouldThrowExceptionWhenAuthenticatedUserNotFoundInDatabase() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(userDetails.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentAuthenticatedUserDto())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id : '1'");
    }
}