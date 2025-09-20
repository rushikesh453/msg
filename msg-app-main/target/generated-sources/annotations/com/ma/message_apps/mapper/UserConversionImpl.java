package com.ma.message_apps.mapper;

import com.ma.message_apps.dto.UserDto;
import com.ma.message_apps.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-07T23:05:28+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class UserConversionImpl implements UserConversion {

    @Override
    public UserDto toUserDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setUserId( user.getUserId() );
        userDto.setCreatedAt( timestampToLocalDateTime( user.getCreatedAt() ) );
        userDto.setUserStatus( user.getStatus() );
        userDto.setUsername( user.getUsername() );
        userDto.setEmail( user.getEmail() );
        userDto.setPasswordHash( user.getPasswordHash() );

        return userDto;
    }

    @Override
    public User toUser(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        User user = new User();

        user.setUserId( userDto.getUserId() );
        user.setCreatedAt( localDateTimeToTimestamp( userDto.getCreatedAt() ) );
        user.setStatus( userDto.getUserStatus() );
        user.setUsername( userDto.getUsername() );
        user.setPasswordHash( userDto.getPasswordHash() );
        user.setEmail( userDto.getEmail() );

        return user;
    }
}
