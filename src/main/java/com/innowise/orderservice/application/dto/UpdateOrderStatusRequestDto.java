package com.innowise.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.innowise.orderservice.domain.model.OrderStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * Received via Kafka
 * It should belong to the infrastructure and be used as an Inbox entity
 */
public record UpdateOrderStatusRequestDto(
    @JsonProperty("order_id") Long orderId,
    @JsonProperty("order_status") OrderStatus orderStatus
) {
}
