package com.orderprocessing.inventory.kafka;

import com.orderprocessing.common.dto.InventoryCheckResultEvent;
import com.orderprocessing.common.dto.OrderCreatedEvent;
import com.orderprocessing.common.dto.OrderProductEvent;
import com.orderprocessing.common.dto.RejectedItem;
import com.orderprocessing.inventory.mock.CatalogMockData;
import com.orderprocessing.inventory.mock.ProductData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductsConsumerListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CatalogMockData catalogMockData;
    private final KafkaProducerService kafkaProducerService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "order.created", groupId = "inventory-id")
    public void consume(OrderCreatedEvent event) {
        checkInventory(event);
    }

    public void checkInventory(OrderCreatedEvent order) {
        List<RejectedItem> rejected = new ArrayList<>();

        for (OrderProductEvent product : order.getProducts()) {
            ProductData productData = catalogMockData.getProduct(product.getId());

            if (productData == null) {
                rejected.add(RejectedItem.builder()
                        .productId(product.getId())
                        .missingQuantity(product.getQuantity())
                        .reason("PRODUCT DOES NOT EXIST")
                        .build());
                continue;
            }

            if(!productData.getCategory().equalsIgnoreCase(product.getCategory())){
                rejected.add(RejectedItem.builder()
                        .productId(product.getId())
                        .missingQuantity(product.getQuantity())
                        .reason("INVALID CATEGORY")
                        .build());
                continue;
            }

            switch (product.getCategory()) {
                case "standard":
                    if (productData.getAvailableQuantity() < product.getQuantity()) {
                        rejected.add(RejectedItem.builder()
                                .productId(product.getId())
                                .missingQuantity(product.getQuantity() - productData.getAvailableQuantity())
                                .build());
                    }
                    break;

                case "perishable":
                    if ((productData.getExpirationDate() != null && productData.getExpirationDate().isBefore(LocalDate.now())) ||
                            productData.getAvailableQuantity() < product.getQuantity()) {
                        rejected.add(RejectedItem.builder()
                                .productId(product.getId())
                                .missingQuantity(product.getQuantity() - productData.getAvailableQuantity())
                                .reason(productData.getExpirationDate().isBefore(LocalDate.now())? "EXPIRED": "")
                                .build());
                    }
                    break;

                default:
                    break;
            }
        }

        String status = rejected.isEmpty() ? "APPROVED" : "REJECTED";

        InventoryCheckResultEvent result = InventoryCheckResultEvent.builder()
                .orderId(order.getOrderId())
                .status(status)
                .missingItems(rejected)
                .checkedAt(String.valueOf(LocalDate.now()))
                .build();
        try {
            kafkaProducerService.sendKafkaEventWithRetry("inventory.checked", result);
        } catch (Exception e) {
            log.error("Kafka retries failed unexpectedly. Event should be in DLQ.", e);
        }
    }

    @KafkaListener(topics = "order.created-dlt", groupId = "inventory-id-dlt")
    public void handleDlt(OrderCreatedEvent event) {
        log.error("Event failed after retries, sent to DLT: {}", event);
    }
}
