package com.ma.message_apps.restcontroller;

import com.ma.message_apps.dto.MessageDto;
import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.exception.UnauthorizedAccessException;
import com.ma.message_apps.service.MessagingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for messaging functionality
 * Handles friends list retrieval, message exchange between users
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class MessagingRestController {

    private final MessagingService messagingService;

    @Autowired
    public MessagingRestController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    /**
     * Retrieves a list of friends/contacts for a user
     *
     * @param userId ID of the user whose friends to retrieve
     * @param session HTTP session for authorization
     * @return List of user DTOs representing friends
     */
    @GetMapping("/messaging/friends/{userId}")
    public ResponseEntity<List<UserDto>> getFriends(
            @PathVariable Integer userId,
            HttpSession session) {
        try {
            log.info("Retrieving friends list for user ID: {}", userId);
            List<UserDto> friends = messagingService.getFriends(userId, session);
            return ResponseEntity.ok(friends);
        } catch (UnauthorizedAccessException e) {
            log.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            log.error("Error retrieving friends for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Simple endpoint to return friends list for the current session
     */
    @GetMapping("/messaging/friends")
    public ResponseEntity<?> getFriendsForCurrentUser(
            @RequestParam(required = false) Integer userId,
            HttpSession session) {
        try {
            if (userId == null) {
                return ResponseEntity.ok(Map.of("success", true, "friends", List.of()));
            }
            List<UserDto> friends = messagingService.getFriends(userId, session);
            return ResponseEntity.ok(Map.of("success", true, "friends", friends));
        } catch (UnauthorizedAccessException e) {
            log.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving friends: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Retrieves messages between two users
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @param session HTTP session for authorization
     * @param request HTTP request for audit logging
     * @return List of message DTOs
     */
    @GetMapping("/messages/{userId1}/{userId2}")
    public ResponseEntity<List<MessageDto>> getMessages(
            @PathVariable Integer userId1,
            @PathVariable Integer userId2,
            HttpSession session,
            HttpServletRequest request) {
        try {
            log.info("Retrieving messages between users {} and {}", userId1, userId2);
            List<MessageDto> messages = messagingService.getMessagesBetweenUsers(userId1, userId2, session, request);
            return ResponseEntity.ok(messages);
        } catch (UnauthorizedAccessException e) {
            log.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            log.error("Error retrieving messages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Sends a message from one user to another
     *
     * @param fromUserId Sender user ID
     * @param toUserId Recipient user ID
     * @param content Message content
     * @param session HTTP session for authorization
     * @param request HTTP request for audit logging
     * @return Success status
     */
    @PostMapping("/messages/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam Integer fromUserId,
            @RequestParam Integer toUserId,
            @RequestParam String content,
            HttpSession session,
            HttpServletRequest request) {
        Map<String, Object> result = messagingService.sendMessage(fromUserId, toUserId, content, session, request);
        boolean isSuccess = (boolean) result.getOrDefault("success", false);

        HttpStatus status;
        if (!isSuccess) {
            String error = (String) result.getOrDefault("error", "");
            if (error.contains("Not authorized") || error.contains("Unauthorized")) {
                status = HttpStatus.FORBIDDEN;
            } else if (error.contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
        } else {
            status = HttpStatus.CREATED;
        }

        return ResponseEntity.status(status).body(result);
    }

    /**
     * Gets all messages for a user (both sent and received)
     *
     * @param userId User ID
     * @param session HTTP session for authorization
     * @param request HTTP request for audit logging
     * @return List of message DTOs
     */
    @GetMapping("/messages/all/{userId}")
    public ResponseEntity<List<MessageDto>> getAllMessagesForUser(
            @PathVariable Integer userId,
            HttpSession session,
            HttpServletRequest request) {
        try {
            log.info("Retrieving all messages for user {}", userId);
            List<MessageDto> messages = messagingService.getAllMessagesForUser(userId, session, request);
            return ResponseEntity.ok(messages);
        } catch (UnauthorizedAccessException e) {
            log.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            log.error("Error retrieving all messages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
