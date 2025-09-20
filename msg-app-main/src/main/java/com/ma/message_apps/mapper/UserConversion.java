package com.ma.message_apps.mapper;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UserConversion {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "timestampToLocalDateTime")
    @Mapping(target = "userStatus", source = "status")
    UserDto toUserDto(User user);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToTimestamp")
    @Mapping(target = "status", source = "userStatus")
    User toUser(UserDto userDto);

    // Custom mapping methods for date conversion
    @org.mapstruct.Named("timestampToLocalDateTime")
    default LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    @org.mapstruct.Named("localDateTimeToTimestamp")
    default Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }
}
