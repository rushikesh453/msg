package com.ma.message_apps.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ma.message_apps.entity.User;
import com.ma.message_apps.enumDto.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestsDto {
    private Integer requestId;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User sender;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User receiver;
    private FriendStatus status;
    private Timestamp createdAt;
}
