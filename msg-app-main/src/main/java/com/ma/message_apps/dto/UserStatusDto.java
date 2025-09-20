package com.ma.message_apps.dto;

import com.ma.message_apps.enumDto.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusDto {
    private Integer userId;
    private String username;
    private String status; // String representation of status

    // Helper method to create from userId and status enum
    public static UserStatusDto fromUserIdAndStatus(Integer userId, String username, UserStatus status) {
        UserStatusDto dto = new UserStatusDto();
        dto.setUserId(userId);
        dto.setUsername(username);
        dto.setStatus(status.name().toLowerCase());
        return dto;
    }
}
