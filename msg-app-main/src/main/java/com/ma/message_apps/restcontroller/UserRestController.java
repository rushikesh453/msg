package com.ma.message_apps.restcontroller;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.dto.UserStatusDto;
import com.ma.message_apps.enumDto.UserStatus;
import com.ma.message_apps.exception.ResourceNotFoundException;
import com.ma.message_apps.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user operations
 * Provides endpoints for searching and managing users
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Find a user by username or email
     *
     * @param query Username or email to search for
     * @return User DTO if found, otherwise appropriate error response
     */
    @GetMapping("/find")
    public ResponseEntity<Map<String, Object>> findUser(@RequestParam("query") String query) {
        try {
            log.info("Controller: Searching for user with query: {}", query);
            Map<String, Object> result = userService.findUsersByUsernameOrEmail(query);
            boolean isSuccess = (boolean) result.getOrDefault("success", false);
            return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.NOT_FOUND).body(result);
        } catch (Exception e) {
            log.error("Error finding user with query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Error finding user: " + e.getMessage()
                    ));
        }
    }

    /**
     * Get user by ID
     *
     * @param userId User ID
     * @return User DTO
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId) {
        try {
            UserDto user = userService.getUserById(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", user
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error getting user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Error retrieving user: " + e.getMessage()
                    ));
        }
    }

    /**
     * Get all users with optional pagination
     *
     * @param page Page number (optional)
     * @param size Page size (optional)
     * @return List of users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<UserDto> users = userService.getAllUsers(page, size);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "users", users,
                    "count", users.size()
            ));
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Error retrieving users: " + e.getMessage()
                    ));
        }
    }

    /**
     * Update user profile
     *
     * @param userId User ID
     * @param userDto Updated user data
     * @return Updated user information
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Integer userId,
            @RequestBody UserDto userDto) {
        Map<String, Object> result = userService.updateUserProfile(userId, userDto);
        boolean isSuccess = (boolean) result.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * Delete a user account
     *
     * @param userId User ID
     * @return Success/failure message
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer userId) {
        Map<String, Object> result = userService.deleteUserAccount(userId);
        boolean isSuccess = (boolean) result.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * Get the status of a user
     * @param userId User ID
     * @return Status information for the user
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<?> getUserStatus(@PathVariable Integer userId) {
        try {
            UserStatusDto status = userService.getUserStatus(userId);
            return ResponseEntity.ok(status);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting user status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to get user status"));
        }
    }

    /**
     * Update the status of a user
     * @param userId User ID
     * @param statusRequest Request body containing status information
     * @return Updated status information
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Integer userId, @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }

            // Convert string to enum (case-insensitive)
            UserStatus userStatus;
            try {
                userStatus = UserStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + statusStr));
            }

            UserStatusDto updatedStatus = userService.updateUserStatus(userId, userStatus);
            return ResponseEntity.ok(updatedStatus);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating user status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update user status"));
        }
    }

    /**
     * Get status information for all users
     * @return List of user status DTOs
     */
    @GetMapping("/statuses")
    public ResponseEntity<?> getAllUserStatuses() {
        try {
            List<UserStatusDto> statuses = userService.getAllUserStatuses();
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            log.error("Error getting all user statuses: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to get all user statuses"));
        }
    }
}
