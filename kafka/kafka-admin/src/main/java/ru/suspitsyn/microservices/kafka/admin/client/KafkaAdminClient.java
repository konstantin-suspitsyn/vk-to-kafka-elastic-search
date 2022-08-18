package ru.suspitsyn.microservices.kafka.admin.client;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.suspitsyn.microservices.config.KafkaConfigData;
import ru.suspitsyn.microservices.config.RetryConfigData;
import ru.suspitsyn.microservices.kafka.admin.exception.KafkaClientException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Component
public class KafkaAdminClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminClient.class);
    private final KafkaConfigData kafkaConfigData;
    private final RetryConfigData retryConfigData;
    private final AdminClient adminClient;
    private final RetryTemplate retryTemplate;
    private final WebClient webClient;

    public KafkaAdminClient(KafkaConfigData kafkaConfigData, RetryConfigData retryConfigData, AdminClient adminClient, RetryTemplate retryTemplate, WebClient webClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.retryConfigData = retryConfigData;
        this.adminClient = adminClient;
        this.retryTemplate = retryTemplate;
        this.webClient = webClient;
    }

    public void createTopics() {
        CreateTopicsResult createTopicsResult;
        try {
            createTopicsResult = retryTemplate.execute(this::doCreateTopics);
        } catch (Throwable e) {
            throw new KafkaClientException("Превышено кол-во запусков");
        }

        checkTopicsCreated();
    }

    private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
        List<String> topicNames = kafkaConfigData.getTopicNameToCreate();
        LOGGER.info("Создаю {} топиков, попытка {}", topicNames.size(), retryContext.getRetryCount());
        List<NewTopic> kafkaTopics = topicNames.stream().map(topic -> new NewTopic(
                topic.trim(),
                kafkaConfigData.getNumOfPartitions(),
                kafkaConfigData.getReplicationFactor()
        )).collect(Collectors.toList());
        return adminClient.createTopics(kafkaTopics);
    }

    private Collection<TopicListing> getTopics() {
        Collection<TopicListing> topicListingCollection;
        try {
            topicListingCollection = retryTemplate.execute(this::doGetTopics);
        } catch (Throwable e) {
            throw new KafkaClientException("Превышено кол-во запусков", e);
        }
        return topicListingCollection;
    }

    private Collection<TopicListing> doGetTopics(RetryContext retryContext) throws ExecutionException, InterruptedException {
        LOGGER.info("Считываем топики Кафки {}, попытка {}",
                kafkaConfigData.getTopicNameToCreate().toArray(),
                retryContext.getRetryCount());
        Collection<TopicListing> topicListingCollection = adminClient.listTopics().listings().get();
        if (topicListingCollection != null) {
            topicListingCollection.forEach(topicListing -> LOGGER.debug("Topic: {}", topicListing.name()));
        }

        return topicListingCollection;
    }


    public void checkTopicsCreated() {
        Collection<TopicListing> topicListings = getTopics();
        int retryCount = 1;
        Integer maxRetry = retryConfigData.getMaxAttempts();
        Integer multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        for (String topic: kafkaConfigData.getTopicNameToCreate()) {
            while (!isTopicCreated(topicListings, topic)) {
                checkMaxRetry(retryCount++, maxRetry);
                sleep(sleepTimeMs);
                sleepTimeMs *= multiplier;
                topicListings = getTopics();
            }
        }
    }

    private void sleep(Long sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {
            throw new KafkaClientException("Что-то поломалось при ожидании");
        }
    }

    private void checkMaxRetry(int i, Integer maxRetry) {
        if (i > maxRetry) {
            throw new KafkaClientException("Слишком много попыток подключиться к Kafka");
        }
    }

    private boolean isTopicCreated(Collection<TopicListing> topicListings, String topic) {
        if (topicListings == null) {
            return false;
        }
        return topicListings.stream().anyMatch(t -> t.name().equals(topic));
    }

    public void checkSchemaRegistry() {
        int retryCount = 1;
        Integer maxRetry = retryConfigData.getMaxAttempts();
        Integer multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        for (String topic: kafkaConfigData.getTopicNameToCreate()) {
            while (!getSchemaRegistryStatus().is2xxSuccessful()) {
                checkMaxRetry(retryCount++, maxRetry);
                sleep(sleepTimeMs);
                sleepTimeMs *= multiplier;
            }
        }
    }

    private HttpStatus getSchemaRegistryStatus() {
        try {
            return webClient
                    .method(HttpMethod.GET)
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .exchangeToMono(response -> Mono.just(response.statusCode()))
                    .block();
        } catch (Exception e) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

}
