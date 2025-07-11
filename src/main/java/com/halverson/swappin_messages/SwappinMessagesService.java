package com.halverson.swappin_messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SwappinMessagesService {

    KafkaTemplate<String, String> kafkaTemplate;

    String producerTopic;

    ObjectMapper objectMapper = new ObjectMapper();

    public SwappinMessagesService(KafkaTemplate<String, String> kafkaTemplate,
                                  @Value("${swappin-messages.producer-topic}") String producerTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.producerTopic = producerTopic;
    }

    @KafkaListener(topics = "${swappin-messages.consumer-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String message) {
        log.info("Received message: {}", message);
        log.info("Message length: {}", message.length());
        try {
            Thread.sleep(1000);
            MessageSwap messageSwap = objectMapper.readValue(message, MessageSwap.class);
            if (messageSwap != null) {
                if (messageSwap.getStopper() < 10) {
                    messageSwap.setStopper(messageSwap.getStopper() + 1);
                    String updatedMessage = objectMapper.writeValueAsString(messageSwap);
                    log.info("Sending updated message: {}", updatedMessage);
                    CompletableFuture<SendResult<String, String>> result = kafkaTemplate.send(producerTopic, updatedMessage);
                    result.whenComplete((sendResult, ex) -> {
                        if (ex != null) {
                            log.error("Error sending message: {}", ex.getMessage());
                        } else {
                            log.info("Message sent successfully: {}", sendResult.getProducerRecord().value());
                        }
                    });
                } else {
                    log.info("Stopping condition met, not sending message.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
