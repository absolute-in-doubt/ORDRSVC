package com.innowise.orderservice.application.dto;

import com.innowise.orderservice.domain.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderFilter(
        LocalDateTime creationDateFrom,
        LocalDateTime creationDateTo,
        List<OrderStatus> orderStatuses
) {
}
