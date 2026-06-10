package com.innowise.orderservice.application.dto;

import com.innowise.orderservice.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long id,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemResponseDto> items
) {
}
