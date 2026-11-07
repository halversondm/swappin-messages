package com.halverson.swappin_messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.halverson.swappin_messages.dao.MessageStorageDao;
import com.halverson.swappin_messages.entity.MessageStorage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwappinMessagesServiceTest {

    private static final String TOPIC = "producer-topic";

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    MessageStorageDao dao;

    SwappinMessagesService service;

    @BeforeEach
    void setUp() {
        service = new SwappinMessagesService(kafkaTemplate, TOPIC, dao);
    }

    @Test
    void incrementsStopperAndForwardsMessage() throws Exception {
        SendResult<String, String> result = new SendResult<>(new ProducerRecord<>(TOPIC, "v"), null);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(result));
        String incoming = "{\"description\":\"d\",\"stopper\":4}";

        service.consumeMessage(incoming);

        ArgumentCaptor<MessageStorage> stored = ArgumentCaptor.forClass(MessageStorage.class);
        verify(dao).save(stored.capture());
        assertEquals(incoming, stored.getValue().getMessage());

        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(TOPIC), sent.capture());
        MessageSwap forwarded = new ObjectMapper().readValue(sent.getValue(), MessageSwap.class);
        assertEquals("d", forwarded.getDescription());
        assertEquals(5, forwarded.getStopper());
    }

    @Test
    void stopsWhenStopperReachesTen() {
        service.consumeMessage("{\"description\":\"d\",\"stopper\":10}");

        verify(dao).save(any(MessageStorage.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void failedSendIsLoggedNotThrown() {
        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        service.consumeMessage("{\"description\":\"d\",\"stopper\":0}");

        verify(kafkaTemplate).send(eq(TOPIC), anyString());
    }

    @Test
    void nullJsonPayloadIsStoredButNotForwarded() {
        service.consumeMessage("null");

        verify(dao).save(any(MessageStorage.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void invalidJsonIsStoredThenRethrown() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.consumeMessage("not-json"));

        assertTrue(ex.getCause() instanceof com.fasterxml.jackson.core.JsonProcessingException);
        verify(dao).save(any(MessageStorage.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void interruptedWhileSleepingRestoresInterruptFlagAndDoesNotForward() {
        Thread.currentThread().interrupt();
        try {
            service.consumeMessage("{\"description\":\"d\",\"stopper\":1}");

            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }
}
