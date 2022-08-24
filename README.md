# Микросервисы на Java c использованием Kafka и ElasticSearch

## Задача
Изучить как работают микросервисы + как работать с Kafka и Elastic Search

Основной поток данных забирается из VK Stream API. Поскольку, сейчас API работает в тестовом режиме и стримит 1% 
данных из всего потока, были выбраны Top теги 2022 года. По ним собирается информация

Проект на 100% учебный — посмотеть как что работает, что-то поломать, что-то починить

## Как запустить на своем компьютере

1. В папке config создать файл VkToKafkaSecretKeys

```
package ru.suspitsyn.microservices.vk.to.kafka.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class VkToKafkaSecretKeys {
    
    //Ввести свой APP_ID и секретный ключ от VK приложения
    private final Integer APP_ID = ;
    private final String SECRET_KEY = "";

}

```
2. Запустить kafka_cluster.yml через docker-compose
