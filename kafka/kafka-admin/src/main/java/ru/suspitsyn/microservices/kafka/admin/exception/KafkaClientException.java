package ru.suspitsyn.microservices.kafka.admin.exception;

import ru.suspitsyn.microservices.kafka.admin.client.KafkaAdminClient;

public class KafkaClientException extends RuntimeException {

    public KafkaClientException() {

    }

    public KafkaClientException(String message) {
        super(message);
    }

    public KafkaClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
