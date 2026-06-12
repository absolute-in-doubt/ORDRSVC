package com.innowise.orderservice.application.mapper;

import com.innowise.orderservice.application.dto.OrderItemRequestDto;
import com.innowise.orderservice.application.dto.OrderItemResponseDto;
import com.innowise.orderservice.domain.model.OrderItems;
import org.mapstruct.Mapping;

import java.util.List;

public interface OrderItemsMapper {

    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemPrice", source = "item.price")
    OrderItemResponseDto toDto(OrderItems entity);

    List<OrderItemResponseDto> toDtoList(List<OrderItems> entityList);

    @Mapping(target = "id", source = "itemId")
    OrderItems toEntity(OrderItemRequestDto orderItemRequestDto);

    List<OrderItems> toEntityList(List<OrderItemRequestDto> orderItemRequestDtoList);
}
