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
import com.example.minitrello.service.interfaces.AuthService;
import com.example.minitrello.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the AuthService interface.
 * Handles user authentication, registration, and security operations.
 * Manages entity-to-DTO conversion for controllers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final UserService userService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDto registerUser(RegisterRequest registerRequest) {
        log.info("Registering new user with email: {}", registerRequest.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Email already in use: {}", registerRequest.getEmail());
            throw new IllegalArgumentException("Email is already in use");
        }

        // Create new user entity from DTO
        User user = userMapper.toEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.ROLE_USER); // Default role
        user.setIsActive(true);

        // Save user and return as DTO
        User savedUser = userRepository.save(user);
        return userService.toDto(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        log.info("Authenticating user with email: {}", loginRequest.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtUtils.generateToken(authentication);

        // Get user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Build and return response
        return  userMapper.toLoginResponse(userDetails,jwt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateToken(String token) {
        log.debug("Validating JWT token");
        return jwtUtils.validateJwtToken(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentAuthenticatedUserDto() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found in security context");
            throw new IllegalStateException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        return userMapper.toDto(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Long getCurrentAuthenticatedUserId() {
        log.debug("Getting currently authenticated user ID");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found in security context");
            throw new IllegalStateException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Helper method to get the authenticated user entity.
     *
     * @return the authenticated user entity
     */
    @Override
    public User getCurrentAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found in security context");
            throw new IllegalStateException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }
}