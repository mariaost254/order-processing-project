package com.orderprocessing.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.common.dto.InventoryCheckResultEvent;
import com.orderprocessing.common.dto.OrderRequestDTO;
import com.orderprocessing.common.dto.RejectedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCheckConsumer {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "inventory.checked", groupId = "notification-id", containerFactory = "kafkaListenerContainerFactory")
    public void onResult(InventoryCheckResultEvent result) {
        String orderId = "order:" + result.getOrderId();

        Object raw = redisTemplate.opsForValue().get(orderId);
        OrderRequestDTO order = objectMapper.convertValue(raw, OrderRequestDTO.class);
        if (order == null) {
            log.error("Order not found in Redis for ID: {}", result.getOrderId());
            return;
        }

        if (result.getStatus().equalsIgnoreCase("APPROVED")) {
            log.info("Order Approved for {}", order.getCustomerName());
        } else {
            log.info(" Order Rejected for {}, missing:", order.getCustomerName());
            for (RejectedItem item : result.getMissingItems()) {
                if(item.getReason() != null){
                    if(item.getReason().equalsIgnoreCase("EXPIRED")) {
                        log.info("{} is expired", item.getProductId());
                    } else {
                        log.info(item.getReason());
                    }
                }
                log.info("{}: {} units", item.getProductId(), item.getMissingQuantity());
            }
        }
        redisTemplate.delete(orderId);
    }

    @KafkaListener(topics = "inventory.checked-dlt", groupId = "notification-id-dlt")
    public void handleDlt(InventoryCheckResultEvent event) {
        log.error("Event failed after retries, sent to DLT: {}", event);
    }
}
