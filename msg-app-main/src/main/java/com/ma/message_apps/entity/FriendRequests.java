package com.ma.message_apps.entity;


import com.ma.message_apps.enumDto.FriendStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "friend_requests")
public class FriendRequests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", referencedColumnName = "user_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
