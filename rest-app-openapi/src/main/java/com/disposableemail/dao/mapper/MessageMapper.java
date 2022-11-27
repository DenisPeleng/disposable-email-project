package com.disposableemail.dao.mapper;

import com.disposableemail.dao.entity.MessageEntity;
import com.disposableemail.rest.model.Message;
import com.disposableemail.rest.model.Messages;
import org.mapstruct.*;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface MessageMapper {

    @Mapping(target = "downloadUrl",
            expression = "java(String.valueOf(\"/messages/\" + messageEntity.getId() + \"/download\"))")
    @Mapping(target = "text", defaultValue = "")
    @Mapping(target = "html", defaultExpression = "java(new ArrayList<>())")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    Message messageEntityToMessage(MessageEntity messageEntity);

    MessageEntity messageToMessageEntity(Message message);

    default Mono<MessageEntity> messageToMessageEntity(Mono<Message> mono) {
        return mono.map(this::messageToMessageEntity);
    }

    @Mapping(target = "downloadUrl",
            expression = "java(String.valueOf(\"/messages/\" + messageEntity.getId() + \"/download\"))")
    Messages messageEntityToMessages(MessageEntity messageEntity);

    MessageEntity messagesToMessageEntity(Messages messages);

    default Mono<MessageEntity> messagesToMessageEntity(Mono<Messages> mono) {
        return mono.map(this::messagesToMessageEntity);
    }

    @AfterMapping
    default void updateResult(@MappingTarget Message message) {
        message.getFrom().forEach(address -> {
            if (Objects.equals(address.getName(), null)) {
                address.setName("");
            }
        });
        message.getTo().forEach(address -> {
            if (Objects.equals(address.getName(), null)) {
                address.setName("");
            }
        });
        message.getBcc().forEach(address -> {
            if (Objects.equals(address.getName(), null)) {
                address.setName("");
            }
        });
    }

}