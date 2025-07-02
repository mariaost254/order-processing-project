package com.orderprocessing.inventory.mock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductData {
    private String productId;
    private String category;
    private int availableQuantity;
    private LocalDate expirationDate;
}
