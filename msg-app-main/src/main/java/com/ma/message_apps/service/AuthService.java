package com.ma.message_apps.service;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.enumDto.UserStatus;
import com.ma.message_apps.exception.ResourceNotFoundException;
import com.ma.message_apps.mapper.UserConversion;
import com.ma.message_apps.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication service handling user login, registration and session management
 */
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserConversion userConversion;

    @Autowired
    public AuthService(UserRepository userRepository, UserConversion userConversion) {
        this.userRepository = userRepository;
        this.userConversion = userConversion;
    }

    /**
     * Authenticate a user and create session
     *
     * @param userDto User credentials
     * @param session HTTP session
     * @return User data with authentication result
     */
    public Map<String, Object> login(UserDto userDto, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find user by username
            User user = userRepository.findByUsername(userDto.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

            // Basic password verification
            if (!userDto.getPasswordHash().equals(user.getPasswordHash())) {
                log.warn("Failed login attempt for username: {}", userDto.getUsername());
                throw new ResourceNotFoundException("Invalid username or password");
            }

            // Update user status to online
            user.setStatus(UserStatus.ONLINE);
            userRepository.save(user);

            // Store user in session
            UserDto userDtoResponse = userConversion.toUserDto(user);
            session.setAttribute("loggedInUser", userDtoResponse);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());

            // Create success response
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", userDtoResponse);

            log.info("User {} logged in successfully", userDto.getUsername());
        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.warn("Failed login attempt for username: {}", userDto.getUsername());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An error occurred during login");
            log.error("Login error: ", e);
        }

        return response;
    }

    /**
     * Register a new user
     *
     * @param userDto User registration data
     * @return Registration result
     */
    public Map<String, Object> register(UserDto userDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate unique username
            if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
                response.put("success", false);
                response.put("error", "Username already exists");
                return response;
            }

            // Validate unique email
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                response.put("success", false);
                response.put("error", "Email already exists");
                return response;
            }

            // Create and save user (with plaintext password for now - not secure)
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setPasswordHash(userDto.getPasswordHash());
            user.setEmail(userDto.getEmail());
            user.setStatus(UserStatus.OFFLINE);
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            User savedUser = userRepository.save(user);

            // Return success response
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("user", userConversion.toUserDto(savedUser));
            log.info("New user registered: {}", userDto.getUsername());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An error occurred during registration");
            log.error("Registration error: ", e);
        }

        return response;
    }

    /**
     * Get the currently logged in user
     *
     * @param session HTTP session
     * @return Current user data or null
     */
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");

        if (loggedInUser != null) {
            // Check if the user data in session is still valid
            Optional<User> userOpt = userRepository.findById(loggedInUser.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("user", userConversion.toUserDto(user));
                return response;
            }
        }

        response.put("success", false);
        response.put("error", "No authenticated user found");
        return response;
    }

    /**
     * Logout the current user
     *
     * @param session HTTP session
     * @return Logout result
     */
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");

            if (loggedInUser != null) {
                // Update user status to offline
                Optional<User> userOpt = userRepository.findById(loggedInUser.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setStatus(UserStatus.OFFLINE);
                    userRepository.save(user);
                    log.info("User {} logged out", loggedInUser.getUsername());
                }
            }

            // Invalidate the session
            session.invalidate();

            response.put("success", true);
            response.put("message", "Logout successful");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An error occurred during logout");
            log.error("Logout error: ", e);
        }

        return response;
    }

    /**
     * Check if a session is valid
     *
     * @param session HTTP session
     * @return True if session is valid
     */
    public boolean isSessionValid(HttpSession session) {
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }
}
