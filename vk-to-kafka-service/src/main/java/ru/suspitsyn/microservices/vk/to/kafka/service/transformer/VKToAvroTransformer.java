package ru.suspitsyn.microservices.vk.to.kafka.service.transformer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.suspitsyn.microservices.kafka.avro.model.VKStream;

@Component
public class VKToAvroTransformer {
    public VKStream getVkAvroModelFromStatus(String eventType, Long postOwnerId, Long postId,
                                             String eventUrl, String text, Long creationTime) {
        return VKStream.newBuilder()
                .setEventType(eventType)
                .setPostOwnerId(postOwnerId)
                .setPostId(postId)
                .setEventUrl(eventUrl)
                .setText(text)
                .setCreationTime(creationTime)
                .build();

    }

}
