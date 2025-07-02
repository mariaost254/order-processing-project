package com.orderprocessing.order.service;


import com.orderprocessing.common.dto.OrderCreatedEvent;
import com.orderprocessing.common.dto.OrderProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.orderprocessing.common.dto.OrderRequestDTO;
import com.orderprocessing.order.utils.OrderProcessingException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {
    private final RedisTemplate<String, Object> redisTemplate;
     private final KafkaProducerService kafkaProducerService;

    @SneakyThrows
    public String setOrdersByClient(OrderRequestDTO orderRequestDTO){
        String orderId = UUID.randomUUID().toString();
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customer(orderRequestDTO.getCustomerName())
                .timestamp(orderRequestDTO.getRequestedAt())
                .products(orderRequestDTO.getItems().stream()
                        .map(item -> OrderProductEvent.builder()
                                .id(item.getProductId())
                                .quantity(item.getQuantity())
                                .category(item.getCategory())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        orderRequestDTO.setStatus("PENDING");

        try {
            redisTemplate.opsForValue().set("order:" + orderId, orderRequestDTO);
        } catch (Exception e) {
            log.error("Failed to save order {} to Redis. Sending to DLQ", orderId, e);
            kafkaProducerService.sendToDlq("order.created.dlq", event);
            throw new OrderProcessingException("We cannot process your order right now. Please try again later.");
        }

        try {
           kafkaProducerService.sendKafkaEventWithRetry("order.created", event);
        } catch (Exception e) {
            log.error("Kafka retries failed unexpectedly for order {}. Event should be in DLQ.", orderId, e);
            throw new OrderProcessingException("We cannot process your order right now. Please try again later.");
        }

        return orderId;
    }
}
