package com.orderprocessing.order.service;

import com.orderprocessing.common.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.RetriableException;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Retryable(
            value = { KafkaException.class, ExecutionException.class, TimeoutException.class, RetriableException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void sendKafkaEventWithRetry(String topic, OrderCreatedEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(topic, event).get();
    }

    @Recover
    public void recover(Exception e, String topic, OrderCreatedEvent event) {
        log.error("Kafka retries exhausted. Sending to DLQ");
        sendToDlq(topic + ".dlq", event);
    }

    public void sendToDlq(String dlqTopic, OrderCreatedEvent failedEvent) {
        try {
            kafkaTemplate.send(dlqTopic, failedEvent);
        } catch (Exception ex) {
            log.error("Critical: Failed to send to DLQ {}: {}", dlqTopic, ex.getMessage(), ex);
        }
    }
}
