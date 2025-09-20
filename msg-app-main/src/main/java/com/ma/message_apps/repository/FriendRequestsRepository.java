package com.ma.message_apps.repository;

import com.ma.message_apps.entity.FriendRequests;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.enumDto.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FriendRequestsRepository extends JpaRepository<FriendRequests,Integer> {
    List<FriendRequests> findByReceiver_UserId(Integer userId);
    boolean existsBySender_UserIdAndReceiver_UserId(Integer senderId, Integer receiverId);

    // Find friend request by sender, receiver and status
    Optional<FriendRequests> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendStatus status);

    @Query("SELECT fr.sender FROM FriendRequests fr WHERE fr.receiver.userId = :userId AND fr.status = com.ma.message_apps.enumDto.FriendStatus.ACCEPTED UNION SELECT fr.receiver FROM FriendRequests fr WHERE fr.sender.userId = :userId AND fr.status = com.ma.message_apps.enumDto.FriendStatus.ACCEPTED")
    List<User> findAcceptedFriends(@Param("userId") Integer userId);

    List<FriendRequests> findByReceiverUserIdAndStatus(Integer userId, FriendStatus friendStatus);

    Optional<FriendRequests> findBySenderUserIdAndReceiverUserId(Integer fromUserId, Integer toUserId);

    /**
     * Check if there's a pending friend request between two users
     */
    @Query("SELECT COUNT(fr) > 0 FROM FriendRequests fr WHERE " +
           "((fr.sender.userId = :fromUserId AND fr.receiver.userId = :toUserId) OR " +
           "(fr.sender.userId = :toUserId AND fr.receiver.userId = :fromUserId)) " +
           "AND fr.status = com.ma.message_apps.enumDto.FriendStatus.PENDING")
    boolean existsPendingRequest(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    /**
     * Create a new friend request
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO friend_requests (sender_id, receiver_id, status, created_at) " +
           "VALUES (:fromUserId, :toUserId, 'PENDING', CURRENT_TIMESTAMP)", nativeQuery = true)
    void insertFriendRequest(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    /**
     * Helper method to create a friend request
     */
    default boolean createFriendRequest(Integer fromUserId, Integer toUserId) {
        try {
            insertFriendRequest(fromUserId, toUserId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find pending friend requests for a user
     */
    @Query("SELECT new map(fr.id as requestId, " +
           "fr.sender.userId as senderId, " +
           "fr.sender.username as senderUsername, " +
           "fr.status as status, " +
           "fr.createdAt as createdAt) " +
           "FROM FriendRequests fr " +
           "WHERE fr.receiver.userId = :userId AND fr.status = com.ma.message_apps.enumDto.FriendStatus.PENDING " +
           "ORDER BY fr.createdAt DESC")
    List<Map<String, Object>> findPendingRequestsForUser(@Param("userId") Integer userId);

    /**
     * Update friend request status
     */
    @Modifying
    @Transactional
    @Query("UPDATE FriendRequests fr SET fr.status = :status WHERE fr.id = :requestId")
    int updateFriendRequestStatusQuery(@Param("requestId") Integer requestId, @Param("status") FriendStatus status);

    /**
     * Helper method to update friend request status
     */
    default boolean updateFriendRequestStatus(Integer requestId, FriendStatus status) {
        try {
            return updateFriendRequestStatusQuery(requestId, status) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if there's a pending friend request between two users (either direction)
     */
    @Query("SELECT COUNT(fr) > 0 FROM FriendRequests fr WHERE " +
           "((fr.sender.userId = :userId1 AND fr.receiver.userId = :userId2) OR " +
           "(fr.sender.userId = :userId2 AND fr.receiver.userId = :userId1)) " +
           "AND fr.status = com.ma.message_apps.enumDto.FriendStatus.PENDING")
    boolean existsPendingRequestBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
}
