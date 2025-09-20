package com.ma.message_apps.controller;

import com.ma.message_apps.dto.UserDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Hidden
public class MessagingController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        // If user is already logged in, redirect to dashboard
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in by looking for the session attribute
        UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");

        if (loggedInUser != null) {
            // User is authenticated, allow access to dashboard
            return "dashboard";
        } else {
            // User is not authenticated, redirect to login
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to access the dashboard");
            return "redirect:/login";
        }
    }
}
