package com.orderprocessing.inventory.mock;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class CatalogMockData {
    private final Map<String, ProductData> productCatalog = new HashMap<>();

    public CatalogMockData() {
        productCatalog.put("P1001", ProductData.builder().productId("P1001")
                .category("standard")
                .availableQuantity(3)
                .expirationDate(null).build());
        productCatalog.put("P1002", ProductData.builder().productId("P1002")
                .category("perishable")
                .availableQuantity(10)
                .expirationDate(LocalDate.of(2025, 7, 1)).build());
        productCatalog.put("P1003", ProductData.builder().productId("P1003")
                .category("digital")
                .availableQuantity(0)
                .expirationDate(null).build());
    }

    public Map<String, ProductData> getCatalog() {
        return productCatalog;
    }

    public ProductData getProduct(String productId) {
        return productCatalog.get(productId);
    }
}
