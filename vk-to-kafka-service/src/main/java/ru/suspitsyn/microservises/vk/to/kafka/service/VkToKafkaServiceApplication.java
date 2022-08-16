package ru.suspitsyn.microservises.vk.to.kafka.service;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.streaming.responses.GetServerUrlResponse;
import com.vk.api.sdk.streaming.clients.StreamingEventHandler;
import com.vk.api.sdk.streaming.clients.VkStreamingApiClient;
import com.vk.api.sdk.streaming.clients.actors.StreamingActor;
import com.vk.api.sdk.streaming.exceptions.StreamingApiException;
import com.vk.api.sdk.streaming.exceptions.StreamingClientException;
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage;
import com.vk.api.sdk.streaming.objects.StreamingRule;
import com.vk.api.sdk.streaming.objects.responses.StreamingGetRulesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.suspitsyn.microservises.vk.to.kafka.service.config.VkToKafkaConfigurationData;
import ru.suspitsyn.microservises.vk.to.kafka.service.config.VkToKafkaSecretKeys;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class VkToKafkaServiceApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(VkToKafkaServiceApplication.class);

    private final VkToKafkaConfigurationData vkToKafkaConfigurationData;
    private final VkToKafkaSecretKeys vkToKafkaSecretKeys;
    //Create service actor
    private final Integer appId;
    private final String accessToken;

    //Init clients
    private final TransportClient transportClient;
    private final VkApiClient vkClient;
    private final VkStreamingApiClient streamingClient;
    //Get streaming actor
    private final GetServerUrlResponse getServerUrlResponse;
    private final StreamingActor actor;

    public VkToKafkaServiceApplication(VkToKafkaConfigurationData vkToKafkaConfigurationData, VkToKafkaSecretKeys vkToKafkaSecretKeys) throws ClientException, ApiException, StreamingClientException, StreamingApiException {
        this.vkToKafkaConfigurationData = vkToKafkaConfigurationData;
        this.vkToKafkaSecretKeys = vkToKafkaSecretKeys;

        appId = this.vkToKafkaSecretKeys.getAPP_ID();
        accessToken = this.vkToKafkaSecretKeys.getSECRET_KEY();

        this.transportClient = new HttpTransportClient();
        this.vkClient = new VkApiClient(this.transportClient);
        this.streamingClient = new VkStreamingApiClient(transportClient);
        this.getServerUrlResponse  = vkClient.streaming().getServerUrl(new ServiceActor(appId, accessToken)).execute();
        this.actor = new StreamingActor(getServerUrlResponse.getEndpoint(), getServerUrlResponse.getKey());

        StreamingGetRulesResponse response = streamingClient.rules().get(actor).execute();

        System.out.println(Arrays.toString(response.getRules().toArray()));

        deleteAndCreateTags(response);

        response = streamingClient.rules().get(actor).execute();

        System.out.println(Arrays.toString(response.getRules().toArray()));

    }

    /**
     * Удаляет все правила, что есть и вставляет актуальные из application.yml
     */
    private void deleteAndCreateTags(StreamingGetRulesResponse streamingGetRulesResponse) throws StreamingClientException, StreamingApiException {

        Object[] vkKeyWords = vkToKafkaConfigurationData.getVkProgrammingKeywords().toArray();
        Set<String> vkKeyWordsSet = new HashSet<>();

        for (Object key:
             vkKeyWords) {
            vkKeyWordsSet.add((String) key);
        }

        for (StreamingRule streamingRule:
             streamingGetRulesResponse.getRules()) {
            if (vkKeyWordsSet.contains(streamingRule.getValue())) {
                // Delete from Set of KeyWord existing word
                vkKeyWordsSet.remove(streamingRule.getValue());
            } else {
                streamingClient.rules().delete(actor, streamingRule.getTag()).execute();
            }
        }

        if (!vkKeyWordsSet.isEmpty()) {
            for (String vkKey:
                 vkKeyWordsSet) {
                streamingClient.rules().add(actor, vkKey, vkKey).execute();
            }
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(VkToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info(vkToKafkaConfigurationData.getWelcomeMessage());
        LOGGER.info(Arrays.toString(vkToKafkaConfigurationData.getVkProgrammingKeywords().toArray()));

        streamingClient.stream().get(actor, new StreamingEventHandler() {
            @Override
            public void handle(StreamingCallbackMessage message) {
                System.out.println(message);
            }
        }).execute();
    }
}
