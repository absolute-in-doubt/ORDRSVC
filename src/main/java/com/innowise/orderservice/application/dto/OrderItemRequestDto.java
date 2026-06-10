package com.innowise.orderservice.application.dto;

public record OrderItemRequestDto(
        Long itemId,
        Long quantity
) {
}
