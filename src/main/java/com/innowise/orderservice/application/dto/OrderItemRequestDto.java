package com.innowise.orderservice.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequestDto(
        @NotNull Long itemId,
        @NotNull @Positive Long quantity
) {
}
