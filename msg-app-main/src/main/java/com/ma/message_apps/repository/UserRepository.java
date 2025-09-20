package com.ma.message_apps.repository;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.enumDto.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by username containing (for search)
     */
    List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Update user status
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.userId = :userId")
    void updateUserStatus(@Param("userId") Integer userId, @Param("status") UserStatus status);

    /**
     * Find users who are friends with the given user
     */
    @Query("SELECT u FROM User u WHERE u.userId IN " +
           "(SELECT CASE WHEN fr.sender.userId = :userId THEN fr.receiver.userId " +
           "ELSE fr.sender.userId END " +
           "FROM FriendRequests fr " +
           "WHERE (fr.sender.userId = :userId OR fr.receiver.userId = :userId) " +
           "AND fr.status = 'ACCEPTED')")
    List<User> findFriendsByUserId(@Param("userId") Integer userId);

    List<User> findByUsernameContainingOrEmailContaining(String query, String query1);
}
