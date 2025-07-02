package com.orderprocessing.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO{
    @NotBlank
    private String customerName;
    @NotEmpty
    private List<OrderItemDTO> items;
    private String requestedAt;
    private String status;
}
