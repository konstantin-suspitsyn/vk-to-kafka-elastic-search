package ru.suspitsyn.microservises.vk.to.kafka.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vk-to-kafka-service")
public class VkToKafkaConfigurationData {
    private List<String> vkProgrammingKeywords;
    private String welcomeMessage;
}
