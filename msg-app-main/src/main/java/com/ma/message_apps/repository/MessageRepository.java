package com.ma.message_apps.repository;

import com.ma.message_apps.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Integer> {
    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :fromUserId AND m.receiver.userId = :toUserId) OR (m.sender.userId = :toUserId AND m.receiver.userId = :fromUserId) ORDER BY m.createdAt ASC")
    List<Message> findChatMessages(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :userId1 AND m.receiver.userId = :userId2) OR (m.sender.userId = :userId2 AND m.receiver.userId = :userId1) ORDER BY m.createdAt ASC")
    List<Message> findMessagesBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    List<Message> findBySenderUserIdOrReceiverUserId(Integer userId, Integer userId1);
}
