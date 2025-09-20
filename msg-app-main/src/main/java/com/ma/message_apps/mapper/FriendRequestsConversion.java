package com.ma.message_apps.mapper;

import com.ma.message_apps.dto.FriendRequestsDto;
import com.ma.message_apps.entity.FriendRequests;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FriendRequestsConversion {

    FriendRequestsDto fromEntity(FriendRequests friendRequests);
    FriendRequests toEntity(FriendRequestsDto friendRequestsDto);

}
