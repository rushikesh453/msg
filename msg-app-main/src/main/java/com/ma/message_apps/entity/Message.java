package com.ma.message_apps.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Entity
@Table(name = "messages")
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = {"sender", "receiver"})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", referencedColumnName = "user_id")
    private User receiver;

    @Column(name = "message_text")
    private String messageText;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "created_at")
    private Timestamp createdAt;



}
