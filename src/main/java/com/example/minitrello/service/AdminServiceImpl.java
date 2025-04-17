package com.example.minitrello.service;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.interfaces.AdminService;
import com.example.minitrello.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the AdminService interface.
 * Provides admin-specific user management functionality.
 * Handles entity-to-DTO conversion for controllers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDto changeUserRole(Long userId, Role role) {
        log.info("Changing role of user ID {} to {}", userId, role);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update role
        user.setRole(role);

        // Save, map to DTO, and return
        User updatedUser = userRepository.save(user);
        return userService.toDto(updatedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsersDetailed(Pageable pageable) {
        log.info("Getting detailed information for all users with pagination");

        // Fetch all users with pagination and map to DTOs
        return userRepository.findAll(pageable).map(userService::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDto disableUser(Long userId) {
        log.info("Disabling user with ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if attempting to disable an admin
        if (user.getRole() == Role.ROLE_ADMIN) {
            log.warn("Attempt to disable an admin account: {}", userId);
            throw new AccessDeniedException("Cannot disable an admin account");
        }

        // Set user as inactive
        user.setIsActive(false);

        // Save, map to DTO, and return
        User updatedUser = userRepository.save(user);
        return userService.toDto(updatedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDto enableUser(Long userId) {
        log.info("Enabling user with ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Enable user
        user.setIsActive(true);

        // Save, map to DTO, and return
        User updatedUser = userRepository.save(user);
        return userService.toDto(updatedUser);
    }
}