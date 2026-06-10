package com.innowise.orderservice.application.dto;

import java.util.List;

public record UpdateOrderRequestDto(
        List<OrderItemRequestDto> items
) {
}
