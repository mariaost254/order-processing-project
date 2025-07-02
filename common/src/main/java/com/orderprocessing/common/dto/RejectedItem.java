package com.orderprocessing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RejectedItem {
    private String productId;
    private int missingQuantity;
    private String reason;
}
