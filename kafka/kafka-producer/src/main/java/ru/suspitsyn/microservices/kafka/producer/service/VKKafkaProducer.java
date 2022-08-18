package ru.suspitsyn.microservices.kafka.producer.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import ru.suspitsyn.microservices.kafka.avro.model.VKStream;

import javax.annotation.PreDestroy;

@Service
public class VKKafkaProducer implements KafkaProducer<Long, VKStream> {

    private KafkaTemplate<Long, VKStream> kafkaTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(VKKafkaProducer.class);

    public VKKafkaProducer(KafkaTemplate<Long, VKStream> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, Long key, VKStream value) {
        LOGGER.info("Отправлено сообщение {} в топик {}", value, topicName);
        ListenableFuture<SendResult<Long, VKStream>> kafkaFuture = kafkaTemplate.send(topicName, key, value);
        kafkaFuture.addCallback(new ListenableFutureCallback<SendResult<Long, VKStream>>() {
            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("Ошибка отправки сообщения {} в топик {}",
                        value.toString(), topicName, ex);
            }

            @Override
            public void onSuccess(SendResult<Long, VKStream> result) {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                LOGGER.debug("Получил метданные:" +
                        "Topic: {};" +
                        "Partition: {};" +
                        "Offset: {};" +
                        "Timestamp: {}, " +
                        "Time: {}",
                        recordMetadata.topic(),
                        recordMetadata.partition(),
                        recordMetadata.offset(),
                        recordMetadata.timestamp(),
                        System.nanoTime());
            }
        });
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            LOGGER.info("Closing Kafka Producer");
            kafkaTemplate.destroy();
        }
    }

}
