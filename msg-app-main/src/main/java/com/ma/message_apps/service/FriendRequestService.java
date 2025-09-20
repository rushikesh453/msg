package com.ma.message_apps.service;

import com.ma.message_apps.dto.FriendRequestsDto;
import com.ma.message_apps.entity.FriendRequests;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.enumDto.FriendStatus;
import com.ma.message_apps.exception.ResourceNotFoundException;
import com.ma.message_apps.mapper.FriendRequestsConversion;
import com.ma.message_apps.repository.FriendRequestsRepository;
import com.ma.message_apps.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing friend requests operations
 */
@Service
@Slf4j
public class FriendRequestService {

    private final FriendRequestsRepository friendRequestsRepository;
    private final UserRepository userRepository;
    private final FriendRequestsConversion friendRequestsConversion;

    @Autowired
    public FriendRequestService(
            FriendRequestsRepository friendRequestsRepository,
            UserRepository userRepository,
            FriendRequestsConversion friendRequestsConversion) {
        this.friendRequestsRepository = friendRequestsRepository;
        this.userRepository = userRepository;
        this.friendRequestsConversion = friendRequestsConversion;
    }

    /**
     * Get friend requests for a specific user
     *
     * @param userId User ID
     * @return List of friend request DTOs
     */
    public List<FriendRequestsDto> getFriendRequestsForUser(Integer userId) {
        log.info("Getting friend requests for user ID: {}", userId);

        // Validate user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<FriendRequests> requests = friendRequestsRepository.findByReceiverUserIdAndStatus(userId, FriendStatus.PENDING);

        return requests.stream()
            .map(friendRequestsConversion::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Send a friend request from one user to another
     *
     * @param fromUserId Sender user ID
     * @param toUserId Recipient user ID
     * @return Map containing success status and details
     */
    @Transactional
    public Map<String, Object> sendFriendRequest(Integer fromUserId, Integer toUserId) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Sending friend request from user {} to user {}", fromUserId, toUserId);

            // Cannot send request to self
            if (fromUserId.equals(toUserId)) {
                response.put("success", false);
                response.put("error", "Cannot send friend request to yourself");
                return response;
            }

            // Check if sender exists
            User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with ID: " + fromUserId));

            // Check if recipient exists
            User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with ID: " + toUserId));

            // Check if request already exists
            Optional<FriendRequests> existingRequest = friendRequestsRepository
                .findBySenderUserIdAndReceiverUserId(fromUserId, toUserId);

            if (existingRequest.isPresent()) {
                FriendRequests request = existingRequest.get();

                // If already accepted, we're done
                if (request.getStatus() == FriendStatus.ACCEPTED) {
                    response.put("success", true);
                    response.put("message", "Already friends");
                    return response;
                }

                // If rejected, allow sending again
                if (request.getStatus() == FriendStatus.REJECTED) {
                    request.setStatus(FriendStatus.PENDING);
                    request.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    friendRequestsRepository.save(request);
                    response.put("success", true);
                    response.put("message", "Friend request sent again");
                    return response;
                }

                // If pending, don't allow sending again
                response.put("success", false);
                response.put("error", "Friend request already sent");
                return response;
            }

            // Create new friend request
            FriendRequests friendRequest = new FriendRequests();
            friendRequest.setSender(fromUser);
            friendRequest.setReceiver(toUser);
            friendRequest.setStatus(FriendStatus.PENDING);
            friendRequest.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            friendRequestsRepository.save(friendRequest);

            response.put("success", true);
            response.put("message", "Friend request sent successfully");
            response.put("request", friendRequestsConversion.fromEntity(friendRequest));

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to send friend request: " + e.getMessage());
            log.error("Error sending friend request", e);
        }

        return response;
    }

    /**
     * Accept a friend request
     *
     * @param requestId Friend request ID
     * @return Map containing success status and details
     */
    @Transactional
    public Map<String, Object> acceptFriendRequest(Integer requestId) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Accepting friend request with ID: {}", requestId);

            FriendRequests request = friendRequestsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with ID: " + requestId));

            // Only PENDING requests can be accepted
            if (request.getStatus() != FriendStatus.PENDING) {
                response.put("success", false);
                response.put("error", "Friend request is not pending");
                return response;
            }

            // Update status
            request.setStatus(FriendStatus.ACCEPTED);
            request.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            friendRequestsRepository.save(request);

            response.put("success", true);
            response.put("message", "Friend request accepted");
            response.put("request", friendRequestsConversion.fromEntity(request));

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to accept friend request: " + e.getMessage());
            log.error("Error accepting friend request", e);
        }

        return response;
    }

    /**
     * Reject a friend request
     *
     * @param requestId Friend request ID
     * @return Map containing success status and details
     */
    @Transactional
    public Map<String, Object> rejectFriendRequest(Integer requestId) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Rejecting friend request with ID: {}", requestId);

            FriendRequests request = friendRequestsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with ID: " + requestId));

            // Only PENDING requests can be rejected
            if (request.getStatus() != FriendStatus.PENDING) {
                response.put("success", false);
                response.put("error", "Friend request is not pending");
                return response;
            }

            // Update status
            request.setStatus(FriendStatus.REJECTED);
            request.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            friendRequestsRepository.save(request);

            response.put("success", true);
            response.put("message", "Friend request rejected");
            response.put("request", friendRequestsConversion.fromEntity(request));

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to reject friend request: " + e.getMessage());
            log.error("Error rejecting friend request", e);
        }

        return response;
    }

    /**
     * Cancel a friend request that was sent
     *
     * @param requestId Friend request ID
     * @return Map containing success status and details
     */
    @Transactional
    public Map<String, Object> cancelFriendRequest(Integer requestId) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Cancelling friend request with ID: {}", requestId);

            FriendRequests request = friendRequestsRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with ID: " + requestId));

            // Only PENDING requests can be cancelled
            if (request.getStatus() != FriendStatus.PENDING) {
                response.put("success", false);
                response.put("error", "Friend request is not pending");
                return response;
            }

            // Delete the request
            friendRequestsRepository.delete(request);

            response.put("success", true);
            response.put("message", "Friend request cancelled");

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to cancel friend request: " + e.getMessage());
            log.error("Error cancelling friend request", e);
        }

        return response;
    }
}
