package ru.suspitsyn.microservices.vk.to.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.suspitsyn.microservices.config.KafkaConfigData;
import ru.suspitsyn.microservices.kafka.avro.model.VKStream;
import ru.suspitsyn.microservices.kafka.producer.service.KafkaProducer;
import ru.suspitsyn.microservices.vk.to.kafka.service.transformer.VKToAvroTransformer;

@Component
public class KafkaStatusListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaStatusListener.class);
    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducer<Long, VKStream> kafkaProducer;
    private final VKToAvroTransformer vkToAvroTransformer;

    public KafkaStatusListener(KafkaConfigData kafkaConfigData, KafkaProducer<Long, VKStream> kafkaProducer, VKToAvroTransformer vkToAvroTransformer) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducer = kafkaProducer;
        this.vkToAvroTransformer = vkToAvroTransformer;
    }

    public void onMessage(String eventType, Long postOwnerId, Long postId,
                          String eventUrl, String text, Long creationTime) {
        LOGGER.info("Данные получены {}. Отправляем в топик {}",
                text, kafkaConfigData.getTopicName());

        VKStream vkStream = vkToAvroTransformer.getVkAvroModelFromStatus(eventType, postOwnerId, postId, eventUrl, text, creationTime);
        kafkaProducer.send(kafkaConfigData.getTopicName(), postOwnerId, vkStream);

    }

}
