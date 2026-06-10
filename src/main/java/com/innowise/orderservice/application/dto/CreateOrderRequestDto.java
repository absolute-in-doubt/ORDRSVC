package com.innowise.orderservice.application.dto;

import java.util.List;

public record CreateOrderRequestDto(
        List<OrderItemRequestDto> items
) {
}
