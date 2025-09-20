package com.ma.message_apps.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ma.message_apps.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"sender", "receiver"})
public class MessageDto {
    private Integer messageId;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User sender;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User receiver;
    private String messageText;
    private Boolean isRead;
    private Timestamp createdAt;
}
