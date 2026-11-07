package com.halverson.swappin_messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.mock.env.MockEnvironment;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartSwappinTest {

    private static final String TOPIC = "out-topic";

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    private StartSwappin create(String... profiles) {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(profiles);
        return new StartSwappin(env, kafkaTemplate, TOPIC);
    }

    @Test
    void startSendsInitialMessageForProfileOne() throws Exception {
        SendResult<String, String> result = new SendResult<>(new ProducerRecord<>(TOPIC, "v"), null);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(result));

        create("one").start();

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq(TOPIC), payload.capture());
        MessageSwap sent = new ObjectMapper().readValue(payload.getValue(), MessageSwap.class);
        assertEquals("Hello from profile one", sent.getDescription());
        assertEquals(1, sent.getStopper());
    }

    @Test
    void startHandlesFailedSendWithoutThrowing() {
        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker down")));

        create("one").start();

        verify(kafkaTemplate).send(anyString(), anyString());
    }

    @Test
    void startWrapsSynchronousSendErrors() {
        IllegalStateException boom = new IllegalStateException("boom");
        when(kafkaTemplate.send(anyString(), anyString())).thenThrow(boom);

        StartSwappin start = create("one");
        RuntimeException thrown = assertThrows(RuntimeException.class, start::start);

        assertSame(boom, thrown.getCause());
    }

    @Test
    void startDoesNothingForOtherProfiles() {
        create("two").start();

        verify(kafkaTemplate, never()).send(anyString(), anyString());
        verifyNoInteractions(kafkaTemplate);
    }
}
