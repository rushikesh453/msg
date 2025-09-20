package com.ma.message_apps.service;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.dto.UserStatusDto;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.enumDto.UserStatus;
import com.ma.message_apps.exception.ResourceNotFoundException;
import com.ma.message_apps.mapper.UserConversion;
import com.ma.message_apps.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 */
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserConversion userConversion;

    @Autowired
    public UserService(UserRepository userRepository, UserConversion userConversion) {
        this.userRepository = userRepository;
        this.userConversion = userConversion;
    }

    /**
     * Get a user by ID
     *
     * @param userId User ID
     * @return User DTO
     */
    public UserDto getUserById(Integer userId) {
        log.info("Getting user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return userConversion.toUserDto(user);
    }

    /**
     * Find users by username or email
     *
     * @param query Search query (username or email)
     * @return Map containing search results
     */
    public Map<String, Object> findUsersByUsernameOrEmail(String query) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Searching for users with query: {}", query);

            if (query == null || query.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Search query cannot be empty");
                return response;
            }

            // Try exact match first
            Optional<User> exactMatch = userRepository.findByUsername(query);
            if (exactMatch.isPresent()) {
                response.put("success", true);
                response.put("user", userConversion.toUserDto(exactMatch.get()));
                return response;
            }

            // Try email exact match
            exactMatch = userRepository.findByEmail(query);
            if (exactMatch.isPresent()) {
                response.put("success", true);
                response.put("user", userConversion.toUserDto(exactMatch.get()));
                return response;
            }

            // Do partial search
            List<User> users = userRepository.findByUsernameContainingOrEmailContaining(query, query);

            if (users.isEmpty()) {
                response.put("success", false);
                response.put("error", "No users found matching the search criteria");
                return response;
            }

            // Return just the first user for now
            // In a more advanced version, you could return multiple and let the user choose
            response.put("success", true);
            response.put("user", userConversion.toUserDto(users.get(0)));
            response.put("totalResults", users.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error searching for users: " + e.getMessage());
            log.error("Error searching for users", e);
        }

        return response;
    }

    /**
     * Get all users (with optional pagination)
     *
     * @param page Page number
     * @param size Page size
     * @return List of user DTOs
     */
    public List<UserDto> getAllUsers(Integer page, Integer size) {
        log.info("Getting all users with pagination - page: {}, size: {}", page, size);

        List<User> users;

        if (page != null && size != null) {
            // Implement pagination logic here if needed
            // This is a simplified version
            users = userRepository.findAll();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, users.size());

            if (fromIndex < users.size()) {
                users = users.subList(fromIndex, toIndex);
            } else {
                users = Collections.emptyList();
            }
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(userConversion::toUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile information
     *
     * @param userId User ID
     * @param userDto Updated user information
     * @return Map containing update results
     */
    public Map<String, Object> updateUserProfile(Integer userId, UserDto userDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Updating profile for user ID: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Update fields - only if they're provided in the DTO
            if (userDto.getUsername() != null && !userDto.getUsername().isEmpty()) {
                // Check if username is already taken by another user
                Optional<User> existingUser = userRepository.findByUsername(userDto.getUsername());
                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                    response.put("success", false);
                    response.put("error", "Username already taken");
                    return response;
                }
                user.setUsername(userDto.getUsername());
            }

            if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
                // Check if email is already taken by another user
                Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                    response.put("success", false);
                    response.put("error", "Email already taken");
                    return response;
                }
                user.setEmail(userDto.getEmail());
            }

            if (userDto.getPasswordHash() != null && !userDto.getPasswordHash().isEmpty()) {
                // Encrypt password
                user.setPasswordHash(userDto.getPasswordHash());
            }

            // Update other profile fields as needed

            User updatedUser = userRepository.save(user);
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", userConversion.toUserDto(updatedUser));

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to update profile: " + e.getMessage());
            log.error("Error updating user profile", e);
        }

        return response;
    }

    /**
     * Delete a user account
     *
     * @param userId User ID
     * @return Map containing deletion results
     */
    public Map<String, Object> deleteUserAccount(Integer userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Deleting user account with ID: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Note: In a real system, you might want to:
            // 1. Check for foreign key constraints
            // 2. Implement soft delete instead of hard delete
            // 3. Archive user data for compliance reasons

            userRepository.delete(user);

            response.put("success", true);
            response.put("message", "Account deleted successfully");

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete account: " + e.getMessage());
            log.error("Error deleting user account", e);
        }

        return response;
    }

    /**
     * Authenticate a user with username and password
     *
     * @param username Username or email
     * @param password Raw password for authentication
     * @return UserDto with user information if authentication successful
     * @throws ResourceNotFoundException if user not found or password invalid
     */
    public UserDto authenticateUser(String username, String password) {
        log.info("Authenticating user: {}", username);

        // Find user by username or email
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with username/email: " + username)));

        // Validate password (plain text comparison - not secure but removes security dependency)
        if (!password.equals(user.getPasswordHash())) {
            log.warn("Invalid password attempt for user: {}", username);
            throw new ResourceNotFoundException("Invalid username or password");
        }

        // Update user status to online
        user.setStatus(com.ma.message_apps.enumDto.UserStatus.ONLINE);
        user = userRepository.save(user);

        log.info("User {} authenticated successfully", username);
        return userConversion.toUserDto(user);
    }

    /**
     * Find user by username
     *
     * @param username Username to search for
     * @return Optional UserDto
     */
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userConversion::toUserDto);
    }

    /**
     * Register a new user
     *
     * @param userDto User data for registration
     * @return Registered user data
     */
    public UserDto registerUser(UserDto userDto) {
        // Check if username already exists
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user entity (storing plaintext password - not secure but removes dependencies)
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPasswordHash(userDto.getPasswordHash());
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        user.setStatus(com.ma.message_apps.enumDto.UserStatus.OFFLINE);

        // Save to database
        User savedUser = userRepository.save(user);

        return userConversion.toUserDto(savedUser);
    }

    /**
     * Get the current status of a user
     * @param userId The ID of the user
     * @return UserStatusDto containing the user's status information
     * @throws ResourceNotFoundException if the user is not found
     */
    public UserStatusDto getUserStatus(Integer userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return UserStatusDto.fromUserIdAndStatus(
                user.getUserId(),
                user.getUsername(),
                user.getStatus() != null ? user.getStatus() : UserStatus.OFFLINE
        );
    }

    /**
     * Update the status of a user
     * @param userId The ID of the user
     * @param status The new status
     * @return UserStatusDto containing the updated status information
     * @throws ResourceNotFoundException if the user is not found
     */
    public UserStatusDto updateUserStatus(Integer userId, UserStatus status) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus(status);
        userRepository.save(user);

        log.info("Updated status for user {}: {}", userId, status);

        return UserStatusDto.fromUserIdAndStatus(user.getUserId(), user.getUsername(), status);
    }

    /**
     * Get the status of all users
     * @return List of UserStatusDto objects containing status information for all users
     */
    public List<UserStatusDto> getAllUserStatuses() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> UserStatusDto.fromUserIdAndStatus(
                        user.getUserId(),
                        user.getUsername(),
                        user.getStatus() != null ? user.getStatus() : UserStatus.OFFLINE
                ))
                .collect(Collectors.toList());
    }

}
