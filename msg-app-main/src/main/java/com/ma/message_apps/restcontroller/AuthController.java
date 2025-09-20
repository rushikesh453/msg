package com.ma.message_apps.restcontroller;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserDto userDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            UserDto registeredUser = userService.registerUser(userDto);

            // Remove password from response
            registeredUser.setPasswordHash(null);

            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", registeredUser);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * User login endpoint - Simple session-based authentication
     */
    @PostMapping("/login")
    public void loginUser(
            @RequestParam String username,
            @RequestParam String passwordHash,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        try {
            // Authenticate user
            UserDto authenticatedUser = userService.authenticateUser(username, passwordHash);

            // Create a session and store user details
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", authenticatedUser);
            session.setAttribute("userId", authenticatedUser.getUserId());
            session.setAttribute("username", authenticatedUser.getUsername());

            // Perform immediate redirect to dashboard
            response.sendRedirect("/dashboard");

        } catch (RuntimeException e) {
            // In case of error, redirect to login page with error message
            response.sendRedirect("/login?error=" + e.getMessage());
        }
    }

    /**
     * Check if user is logged in
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        UserDto user = (UserDto) session.getAttribute("loggedInUser");
        if (user != null) {
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "No authenticated user found");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get user before invalidating session
            UserDto user = (UserDto) session.getAttribute("loggedInUser");

            // Invalidate session
            session.invalidate();

            // Update user status if needed
            if (user != null) {
                userService.updateUserStatus(user.getUserId(), com.ma.message_apps.enumDto.UserStatus.OFFLINE);
            }

            response.put("success", true);
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error during logout: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Endpoint for direct server-side login with redirect
     */
    @PostMapping("/direct-login")
    public void directLogin(
            @RequestParam String username,
            @RequestParam String passwordHash,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        try {
            // Authenticate user
            UserDto authenticatedUser = userService.authenticateUser(username, passwordHash);

            // Create a session and store user details
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", authenticatedUser);
            session.setAttribute("userId", authenticatedUser.getUserId());
            session.setAttribute("username", authenticatedUser.getUsername());

            // Perform immediate redirect to dashboard
            response.sendRedirect("/dashboard");
        } catch (RuntimeException e) {
            // In case of error, redirect to login page with error message
            response.sendRedirect("/login?error=true");
        }
    }
}
