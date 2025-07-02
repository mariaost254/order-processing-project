package com.orderprocessing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryCheckResultEvent {
    private String orderId;
    private String status; // "APPROVED" or "REJECTED"
    private List<RejectedItem> missingItems;
    private String checkedAt;

}
