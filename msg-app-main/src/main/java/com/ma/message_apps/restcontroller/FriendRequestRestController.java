package com.ma.message_apps.restcontroller;

import com.ma.message_apps.dto.FriendRequestsDto;
import com.ma.message_apps.service.FriendRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for friend request operations
 */
@RestController
@RequestMapping("/api/friend-requests")
@Slf4j
public class FriendRequestRestController {

    private final FriendRequestService friendRequestService;

    @Autowired
    public FriendRequestRestController(FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    /**
     * Get all friend requests for the current user (from session)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFriendRequests(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get user ID from session
            Object userIdObj = session.getAttribute("userId");

            if (userIdObj != null) {
                Integer userId = (Integer) userIdObj;
                List<FriendRequestsDto> requests = friendRequestService.getFriendRequestsForUser(userId);

                response.put("success", true);
                response.put("requests", requests);
            } else {
                // Return empty list if no user in session
                response.put("success", true);
                response.put("requests", Collections.emptyList());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching friend requests", e);
            response.put("success", false);
            response.put("error", "Error fetching friend requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get friend requests for a specific user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getFriendRequests(@PathVariable Integer userId) {
        try {
            List<FriendRequestsDto> requests = friendRequestService.getFriendRequestsForUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "requests", requests
            ));
        } catch (Exception e) {
            log.error("Error fetching friend requests for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error fetching friend requests: " + e.getMessage()
            ));
        }
    }

    /**
     * Send a friend request
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendFriendRequest(
            @RequestParam Integer fromUserId,
            @RequestParam Integer toUserId) {
        Map<String, Object> result = friendRequestService.sendFriendRequest(fromUserId, toUserId);
        boolean isSuccess = (boolean) result.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(result);
    }

    /**
     * Accept a friend request
     */
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptFriendRequest(@RequestParam Integer requestId) {
        Map<String, Object> result = friendRequestService.acceptFriendRequest(requestId);
        boolean isSuccess = (boolean) result.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(result);
    }

    /**
     * Reject a friend request
     */
    @PostMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectFriendRequest(@RequestParam Integer requestId) {
        Map<String, Object> result = friendRequestService.rejectFriendRequest(requestId);
        boolean isSuccess = (boolean) result.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(result);
    }

    /**
     * Cancel a sent friend request
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelFriendRequest(@RequestParam Integer requestId) {
        Map<String, Object> result = friendRequestService.cancelFriendRequest(requestId);
        boolean isSuccess = (boolean) result.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(result);
    }
}
