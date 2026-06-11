package com.innowise.orderservice.application.mapper;

import com.innowise.orderservice.application.dto.OrderResponseDto;
import com.innowise.orderservice.domain.model.Order;

public interface OrderMapper {

    OrderResponseDto toDto(Order entity);
}
