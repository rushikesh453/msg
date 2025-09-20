package com.ma.message_apps.service;

import com.ma.message_apps.dto.MessageDto;
import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.entity.Message;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.exception.ResourceNotFoundException;
import com.ma.message_apps.mapper.MessageConversion;
import com.ma.message_apps.mapper.UserConversion;
import com.ma.message_apps.repository.FriendRequestsRepository;
import com.ma.message_apps.repository.MessageRepository;
import com.ma.message_apps.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service handling message-related business logic
 */
@Service
@Slf4j
public class MessagingService {

    private final UserRepository userRepository;
    private final FriendRequestsRepository friendRequestsRepository;
    private final MessageRepository messageRepository;
    private final UserConversion userConversion;
    private final MessageConversion messageConversion;

    @Autowired
    public MessagingService(
            UserRepository userRepository,
            FriendRequestsRepository friendRequestsRepository,
            MessageRepository messageRepository,
            UserConversion userConversion,
            MessageConversion messageConversion) {
        this.userRepository = userRepository;
        this.friendRequestsRepository = friendRequestsRepository;
        this.messageRepository = messageRepository;
        this.userConversion = userConversion;
        this.messageConversion = messageConversion;
    }

    /**
     * Get list of friends for a user
     *
     * @param userId ID of the user
     * @param session HTTP session for authorization
     * @return List of user DTOs representing friends
     */
    public List<UserDto> getFriends(Integer userId, HttpSession session) {
        log.info("Service: Retrieving friends list for user ID: {}", userId);

        // Validate user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<User> friends = friendRequestsRepository.findAcceptedFriends(userId);
        return friends.stream()
            .map(userConversion::toUserDto)
            .collect(Collectors.toList());
    }

    /**
     * Get messages between two users
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @param session HTTP session for authorization
     * @param request HTTP request for logging
     * @return List of message DTOs
     */
    public List<MessageDto> getMessagesBetweenUsers(
            Integer userId1,
            Integer userId2,
            HttpSession session,
            HttpServletRequest request) {
        log.info("Service: Retrieving messages between users {} and {}", userId1, userId2);

        // Validate both users exist
        User user1 = userRepository.findById(userId1)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId1));

        User user2 = userRepository.findById(userId2)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId2));

        // Get messages in both directions
        List<Message> messages = messageRepository.findMessagesBetweenUsers(userId1, userId2);

        return messages.stream()
            .map(messageConversion::toMessageDto)
            .collect(Collectors.toList());
    }

    /**
     * Send a message from one user to another
     *
     * @param fromUserId Sender user ID
     * @param toUserId Recipient user ID
     * @param content Message content
     * @param session HTTP session for authorization
     * @param request HTTP request for logging
     * @return Map containing success status and message details
     */
    @Transactional
    public Map<String, Object> sendMessage(
            Integer fromUserId,
            Integer toUserId,
            String content,
            HttpSession session,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Service: Sending message from user {} to user {}", fromUserId, toUserId);

            // Validate content
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Message content cannot be empty");
                return response;
            }

            // Get sender user
            User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with ID: " + fromUserId));

            // Get recipient user
            User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with ID: " + toUserId));

            // Verify these users are friends (optional based on your requirements)
            /*
            boolean areFriends = !friendRequestsRepository
                .findAcceptedFriendship(fromUserId, toUserId).isEmpty();
            if (!areFriends) {
                response.put("success", false);
                response.put("error", "Cannot send message to a non-friend user");
                return response;
            }
            */

            // Create and save message
            Message message = new Message();
            message.setSender(fromUser);
            message.setReceiver(toUser);
            message.setMessageText(content);
            message.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            Message savedMessage = messageRepository.save(message);

            response.put("success", true);
            response.put("message", messageConversion.toMessageDto(savedMessage));

        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn(e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to send message: " + e.getMessage());
            log.error("Error sending message", e);
        }

        return response;
    }

    /**
     * Get all messages for a user (both sent and received)
     *
     * @param userId User ID
     * @param session HTTP session for authorization
     * @param request HTTP request for logging
     * @return List of message DTOs
     */
    public List<MessageDto> getAllMessagesForUser(
            Integer userId,
            HttpSession session,
            HttpServletRequest request) {
        log.info("Service: Retrieving all messages for user {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Message> messages = messageRepository.findBySenderUserIdOrReceiverUserId(userId, userId);

        return messages.stream()
            .map(messageConversion::toMessageDto)
            .collect(Collectors.toList());
    }
}
