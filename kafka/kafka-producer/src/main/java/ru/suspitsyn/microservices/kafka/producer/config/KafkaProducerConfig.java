package ru.suspitsyn.microservices.kafka.producer.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.suspitsyn.microservices.config.KafkaConfigData;
import ru.suspitsyn.microservices.config.KafkaProducerConfigData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig <K extends Serializable, V extends SpecificRecordBase> {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducerConfigData kafkaProducerConfigData;

    public KafkaProducerConfig(KafkaConfigData kafkaConfigData, KafkaProducerConfigData kafkaProducerConfigData) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducerConfigData = kafkaProducerConfigData;
    }

    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        properties.put(kafkaConfigData.getSchemaRegistryUrlKey(), kafkaConfigData.getSchemaRegistryUrl());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerConfigData.getKeySerializerClass());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerConfigData.getValueSerializerClass());
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG,
                kafkaProducerConfigData.getBatchSize() * kafkaProducerConfigData.getBatchSizeBoostFactor());
        properties.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProducerConfigData.getLingerMs());
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProducerConfigData.getCompressionType());
        properties.put(ProducerConfig.ACKS_CONFIG, kafkaProducerConfigData.getAcks());

        return properties;

    }

    @Bean
    public KafkaTemplate<K, V> kafkaTemplate() {
        return new  KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<K, V> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }
}
