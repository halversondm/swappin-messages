package com.halverson.swappin_messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StartSwappin {

    Environment environment;
    KafkaTemplate<String, String> kafkaTemplate;
    String topic;
    ObjectMapper objectMapper = new ObjectMapper();

    public StartSwappin(Environment environment, KafkaTemplate<String, String> kafkaTemplate, @Value("swappin-messages.producer-topic") String topic) {
        this.environment = environment;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    void start() {
        if (environment.matchesProfiles("one")) {
            try {
                MessageSwap messageSwap = new MessageSwap("Hello from profile one", 1);
                String message = objectMapper.writeValueAsString(messageSwap);
                kafkaTemplate.send(topic, message).whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        log.error("Error sending message: " + ex.getMessage());
                    } else {
                        log.info("Message sent successfully: " + sendResult.getProducerRecord().value());
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
