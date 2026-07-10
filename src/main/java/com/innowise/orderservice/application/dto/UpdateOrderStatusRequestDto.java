package com.innowise.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.innowise.orderservice.domain.model.OrderStatus;

/**
 * Received via Kafka
 * It should belong to the infrastructure and be used as an Inbox entity
 */
public record UpdateOrderStatusRequestDto(
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("order_status") OrderStatus orderStatus
) {
}
