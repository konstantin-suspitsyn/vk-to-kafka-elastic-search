package ru.suspitsyn.microservices.vk.to.kafka.service.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.suspitsyn.microservices.config.KafkaConfigData;
import ru.suspitsyn.microservices.kafka.admin.client.KafkaAdminClient;

@Component
public class KafkaStreamInitializer implements StreamInitializer {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaStreamInitializer.class);

    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    public KafkaStreamInitializer(KafkaConfigData kafkaConfigData, KafkaAdminClient kafkaAdminClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaAdminClient = kafkaAdminClient;
    }

    @Override
    public void init() {
        kafkaAdminClient.createTopics();
        kafkaAdminClient.checkSchemaRegistry();
        LOGGER.info("Kafka готова работать");
    }
}
