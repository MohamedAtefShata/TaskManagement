package com.example.minitrello.service;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.dto.user.UserUpdateDto;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.UserMapper;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of the UserService interface.
 * Provides user management functionality.
 * Handles entity-to-DTO conversion for controllers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);

        User updatedUser = userRepository.findById(id)
                .map(existingUser -> {
                    // Apply DTO fields to entity
                    userMapper.updateUserFromDto(updateDto, existingUser);

                    // Handle password separately due to encoding
                    if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(updateDto.getPassword()));
                    }

                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return toDto(updatedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findDtoById(Long id) {
        log.debug("Finding user DTO by ID: {}", id);
        return userRepository.findById(id).map(this::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAllUser(Pageable pageable) {
        log.debug("Finding all user DTOs with pagination");
        return userRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists with email: {}", email);
        return userRepository.existsByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return userMapper.toDto(user);
    }
}