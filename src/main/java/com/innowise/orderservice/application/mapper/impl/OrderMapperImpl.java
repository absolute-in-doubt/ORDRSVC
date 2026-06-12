package com.innowise.orderservice.application.mapper.impl;

import com.innowise.orderservice.application.dto.OrderItemResponseDto;
import com.innowise.orderservice.application.dto.OrderResponseDto;
import com.innowise.orderservice.application.dto.UpdateOrderRequestDto;
import com.innowise.orderservice.application.mapper.OrderItemsMapper;
import com.innowise.orderservice.application.mapper.OrderMapper;
import com.innowise.orderservice.domain.model.Order;
import com.innowise.orderservice.domain.model.OrderItems;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapperImpl implements OrderMapper {

    private final OrderItemsMapper orderItemsMapper;

    @Override
    public OrderResponseDto toDto(Order entity) {
        return new OrderResponseDto(
                entity.getId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getTotalPrice(),
                orderItemsMapper.toDtoList(entity.getItems())
        );
    }

    @Override
    public void updateEntity(UpdateOrderRequestDto updateRequest, Order order) {
        order.setItems(orderItemsMapper.toEntityList(updateRequest.items()));
    }
}
