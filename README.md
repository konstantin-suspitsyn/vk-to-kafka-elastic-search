# Чтобы работало

В папке config создать файл VkToKafkaSecretKeys

```
package ru.suspitsyn.microservises.vk.to.kafka.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class VkToKafkaSecretKeys {
    
    //Ввести свой APP_ID и секретный ключ
    private final Integer APP_ID = ;
    private final String SECRET_KEY = "";

}

```