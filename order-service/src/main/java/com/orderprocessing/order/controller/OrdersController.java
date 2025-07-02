package com.orderprocessing.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.orderprocessing.common.dto.OrderRequestDTO;
import com.orderprocessing.order.service.OrdersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    public ResponseEntity<String> ordersByClient(@Valid  @RequestBody OrderRequestDTO orderRequestDTO){
        orderRequestDTO.setRequestedAt(String.valueOf(LocalDate.now()));
        String orderId = ordersService.setOrdersByClient(orderRequestDTO);
        return ResponseEntity.ok("Order created with ID: " + orderId);
    }
}
