package com.example.minitrello.service;

import com.example.minitrello.exception.ResourceNotFoundException;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with ID: {}", id);

        return userRepository.findById(id)
                .map(existingUser -> {
                    // Update fields that can be updated
                    existingUser.setName(userDetails.getName());

                    // Only update password if a new one is provided
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }

                    // Save and return updated user
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
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
    public Page<User> findAllUsers(Pageable pageable) {
        log.debug("Finding all users with pagination");
        return userRepository.findAll(pageable);
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
}