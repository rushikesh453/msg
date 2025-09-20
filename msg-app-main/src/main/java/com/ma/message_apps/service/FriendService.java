package com.ma.message_apps.service;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.exception.ResourceNotFoundException;
import com.ma.message_apps.mapper.UserConversion;
import com.ma.message_apps.repository.FriendRequestsRepository;
import com.ma.message_apps.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling friend-related operations
 */
@Service
@Slf4j
public class FriendService {

    private final FriendRequestsRepository friendRequestsRepository;
    private final UserRepository userRepository;
    private final UserConversion userConversion;

    @Autowired
    public FriendService(
            FriendRequestsRepository friendRequestsRepository,
            UserRepository userRepository,
            UserConversion userConversion) {
        this.friendRequestsRepository = friendRequestsRepository;
        this.userRepository = userRepository;
        this.userConversion = userConversion;
    }

    /**
     * Get user's friend list from session or specified user ID
     *
     * @param session HTTP session
     * @param specifiedUserId Optional user ID (if null, uses session user)
     * @return Map containing success status and friends list
     */
    public Map<String, Object> getFriendsList(HttpSession session, Integer specifiedUserId) {
        Map<String, Object> response = new HashMap<>();
        List<UserDto> friendsList = new ArrayList<>();

        try {
            // Don't modify this variable after initialization if used in lambda
            final Integer userId;

            // If no specified user ID, try to get from session
            if (specifiedUserId == null) {
                Object userIdObj = session.getAttribute("userId");
                if (userIdObj == null || !(userIdObj instanceof Integer)) {
                    response.put("success", true);
                    response.put("friends", friendsList);
                    response.put("message", "No authenticated user in session");
                    return response;
                }
                userId = (Integer) userIdObj;
            } else {
                userId = specifiedUserId;
            }

            // Validate user exists
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Get friends from repository
            List<User> friends = friendRequestsRepository.findAcceptedFriends(userId);

            if (friends == null) {
                friends = new ArrayList<>();
            }

            // Convert to DTOs - using a safer approach to handle potential nulls
            for (User friend : friends) {
                if (friend != null) {
                    try {
                        UserDto dto = userConversion.toUserDto(friend);
                        if (dto != null) {
                            friendsList.add(dto);
                        }
                    } catch (Exception e) {
                        log.error("Error converting user to DTO: " + e.getMessage());
                    }
                }
            }

            response.put("success", true);
            response.put("friends", friendsList);

            log.info("Retrieved {} friends for user ID: {}", friendsList.size(), userId);

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve friends list: " + e.getMessage());
            log.error("Error retrieving friends list", e);
        }

        return response;
    }

    /**
     * Check if users are friends
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return true if users are friends, false otherwise
     */
    public boolean areFriends(Integer userId1, Integer userId2) {
        try {
            List<User> friendsOfUser1 = friendRequestsRepository.findAcceptedFriends(userId1);
            if (friendsOfUser1 == null) {
                return false;
            }
            return friendsOfUser1.stream()
                .anyMatch(user -> user != null && user.getUserId() != null && user.getUserId().equals(userId2));
        } catch (Exception e) {
            log.error("Error checking friendship: " + e.getMessage());
            return false;
        }
    }

    /**
     * Find a user by username or email
     *
     * @param usernameOrEmail The username or email to search for
     * @return Map containing success status and user data if found
     */
    public Map<String, Object> findUserByUsernameOrEmail(String usernameOrEmail) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Username or email cannot be empty");
                return response;
            }

            // First try to find by username
            User user = userRepository.findByUsername(usernameOrEmail)
                .orElse(null);

            // If not found by username, try email
            if (user == null) {
                user = userRepository.findByEmail(usernameOrEmail)
                    .orElse(null);
            }

            if (user == null) {
                response.put("success", false);
                response.put("error", "User not found");
                return response;
            }

            // Convert to DTO
            UserDto userDto = userConversion.toUserDto(user);

            // Don't return the password hash
            if (userDto != null) {
                userDto.setPasswordHash(null);
            }

            response.put("success", true);
            response.put("user", userDto);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error finding user: " + e.getMessage());
            log.error("Error finding user by username/email", e);
        }

        return response;
    }

    /**
     * Send a friend request
     *
     * @param fromUserId The user ID sending the request
     * @param toUserId The user ID receiving the request
     * @return Map containing success status and request details
     */
    public Map<String, Object> sendFriendRequest(Integer fromUserId, Integer toUserId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate user IDs
            if (fromUserId == null || toUserId == null) {
                response.put("success", false);
                response.put("error", "Invalid user IDs");
                return response;
            }

            // Check if users exist
            User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found"));

            User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found"));

            // Check if users are already friends
            if (areFriends(fromUserId, toUserId)) {
                response.put("success", false);
                response.put("error", "Users are already friends");
                return response;
            }

            // Check if there's already a pending request
            boolean existingRequest = friendRequestsRepository.existsPendingRequest(fromUserId, toUserId);
            if (existingRequest) {
                response.put("success", false);
                response.put("error", "Friend request already sent");
                return response;
            }

            // Create friend request
            boolean requestCreated = friendRequestsRepository.createFriendRequest(fromUserId, toUserId);

            if (requestCreated) {
                response.put("success", true);
                response.put("message", "Friend request sent successfully");
            } else {
                response.put("success", false);
                response.put("error", "Failed to send friend request");
            }

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error sending friend request: " + e.getMessage());
            log.error("Error sending friend request", e);
        }

        return response;
    }

    /**
     * Get pending friend requests for a user
     *
     * @param userId The user ID to get requests for
     * @return Map containing success status and list of requests
     */
    public Map<String, Object> getFriendRequests(Integer userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate user ID
            if (userId == null) {
                response.put("success", false);
                response.put("error", "Invalid user ID");
                return response;
            }

            // Check if user exists
            userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Get friend requests
            List<Map<String, Object>> requests = friendRequestsRepository.findPendingRequestsForUser(userId);

            response.put("success", true);
            response.put("requests", requests);

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error retrieving friend requests: " + e.getMessage());
            log.error("Error retrieving friend requests", e);
        }

        return response;
    }

    /**
     * Accept a friend request
     *
     * @param requestId The ID of the friend request to accept
     * @return Map containing success status and result details
     */
    public Map<String, Object> acceptFriendRequest(Integer requestId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request ID
            if (requestId == null) {
                response.put("success", false);
                response.put("error", "Invalid request ID");
                return response;
            }

            // Update the request status in the database
            boolean updated = friendRequestsRepository.updateFriendRequestStatus(requestId, com.ma.message_apps.enumDto.FriendStatus.ACCEPTED);

            if (updated) {
                response.put("success", true);
                response.put("message", "Friend request accepted");
            } else {
                response.put("success", false);
                response.put("error", "Failed to accept friend request");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error accepting friend request: " + e.getMessage());
            log.error("Error accepting friend request", e);
        }

        return response;
    }

    /**
     * Reject a friend request
     *
     * @param requestId The ID of the friend request to reject
     * @return Map containing success status and result details
     */
    public Map<String, Object> rejectFriendRequest(Integer requestId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request ID
            if (requestId == null) {
                response.put("success", false);
                response.put("error", "Invalid request ID");
                return response;
            }

            // Update the request status in the database
            boolean updated = friendRequestsRepository.updateFriendRequestStatus(requestId, com.ma.message_apps.enumDto.FriendStatus.REJECTED);

            if (updated) {
                response.put("success", true);
                response.put("message", "Friend request rejected");
            } else {
                response.put("success", false);
                response.put("error", "Failed to reject friend request");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error rejecting friend request: " + e.getMessage());
            log.error("Error rejecting friend request", e);
        }

        return response;
    }
}
