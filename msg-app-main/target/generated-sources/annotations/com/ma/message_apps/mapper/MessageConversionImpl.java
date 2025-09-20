package com.ma.message_apps.mapper;

import com.ma.message_apps.dto.MessageDto;
import com.ma.message_apps.entity.Message;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-07T23:05:28+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class MessageConversionImpl implements MessageConversion {

    @Override
    public MessageDto toMessageDto(Message message) {
        if ( message == null ) {
            return null;
        }

        MessageDto messageDto = new MessageDto();

        messageDto.setMessageId( message.getMessageId() );
        messageDto.setSender( message.getSender() );
        messageDto.setReceiver( message.getReceiver() );
        messageDto.setMessageText( message.getMessageText() );
        messageDto.setIsRead( message.getIsRead() );
        messageDto.setCreatedAt( message.getCreatedAt() );

        return messageDto;
    }

    @Override
    public Message toMessage(MessageDto messageDto) {
        if ( messageDto == null ) {
            return null;
        }

        Message message = new Message();

        message.setMessageId( messageDto.getMessageId() );
        message.setSender( messageDto.getSender() );
        message.setReceiver( messageDto.getReceiver() );
        message.setMessageText( messageDto.getMessageText() );
        message.setIsRead( messageDto.getIsRead() );
        message.setCreatedAt( messageDto.getCreatedAt() );

        return message;
    }
}
