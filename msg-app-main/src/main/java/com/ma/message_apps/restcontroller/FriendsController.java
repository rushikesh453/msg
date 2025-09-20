package com.ma.message_apps.restcontroller;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.service.FriendService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendsController {

    @Autowired
    private FriendService friendService;

    /**
     * Get user's friend list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getFriendsList(HttpSession session) {
        Map<String, Object> response = friendService.getFriendsList(session, null);
        boolean isSuccess = (boolean) response.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Get friend list for a specific user
     */
    @GetMapping("/list/{userId}")
    public ResponseEntity<Map<String, Object>> getFriendsListForUser(
            @PathVariable Integer userId,
            HttpSession session) {
        Map<String, Object> response = friendService.getFriendsList(session, userId);
        boolean isSuccess = (boolean) response.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Check if users are friends
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkIfFriends(
            @RequestParam Integer userId1,
            @RequestParam Integer userId2) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean areFriends = friendService.areFriends(userId1, userId2);
            response.put("success", true);
            response.put("areFriends", areFriends);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Find a user by username or email
     */
    @GetMapping("/find")
    public ResponseEntity<Map<String, Object>> findUser(@RequestParam String query) {
        Map<String, Object> response = friendService.findUserByUsernameOrEmail(query);
        boolean isSuccess = (boolean) response.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * Send a friend request
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> sendFriendRequest(
            @RequestParam Integer fromUserId,
            @RequestParam Integer toUserId) {
        Map<String, Object> response = friendService.sendFriendRequest(fromUserId, toUserId);
        boolean isSuccess = (boolean) response.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Get friend requests for the current user
     */
    @GetMapping("/requests")
    public ResponseEntity<Map<String, Object>> getFriendRequests(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "No authenticated user found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Map<String, Object> response = friendService.getFriendRequests(userId);
        boolean isSuccess = (boolean) response.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Accept a friend request
     */
    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<Map<String, Object>> acceptFriendRequest(
            @PathVariable Integer requestId,
            HttpSession session) {
        Map<String, Object> response = friendService.acceptFriendRequest(requestId);
        boolean isSuccess = (boolean) response.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Reject a friend request
     */
    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<Map<String, Object>> rejectFriendRequest(
            @PathVariable Integer requestId,
            HttpSession session) {
        Map<String, Object> response = friendService.rejectFriendRequest(requestId);
        boolean isSuccess = (boolean) response.getOrDefault("success", false);
        return ResponseEntity.status(isSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
