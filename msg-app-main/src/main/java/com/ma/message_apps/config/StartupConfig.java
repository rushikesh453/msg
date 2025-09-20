package com.ma.message_apps.config;

import com.ma.message_apps.entity.User;
import com.ma.message_apps.enumDto.UserStatus;
import com.ma.message_apps.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuration that runs at application startup
 * Handles resetting user statuses when application starts
 */
@Component
@Slf4j
public class StartupConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository userRepository;

    @Autowired
    public StartupConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method runs after the application has started
     * It resets all user statuses to OFFLINE
     */
    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application started - Resetting all user statuses to OFFLINE");
        try {
            // Find all users
            Iterable<User> users = userRepository.findAll();

            // Update each user's status to OFFLINE
            for (User user : users) {
                user.setStatus(UserStatus.OFFLINE);
            }

            // Save all users
            userRepository.saveAll(users);
            log.info("Successfully reset all user statuses to OFFLINE");
        } catch (Exception e) {
            log.error("Error resetting user statuses at startup", e);
        }
    }
}
