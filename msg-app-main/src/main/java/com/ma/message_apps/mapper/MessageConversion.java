package com.ma.message_apps.mapper;

import com.ma.message_apps.dto.MessageDto;
import com.ma.message_apps.entity.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageConversion {

    MessageDto toMessageDto(Message message);
    Message toMessage(MessageDto messageDto);
}
