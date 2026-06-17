package com.innowise.orderservice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateOrderRequestDto(
        @NotEmpty @Valid List<OrderItemRequestDto> items
) {
}
