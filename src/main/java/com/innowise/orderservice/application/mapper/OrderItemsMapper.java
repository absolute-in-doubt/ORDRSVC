package com.innowise.orderservice.application.mapper;

import com.innowise.orderservice.application.dto.OrderItemRequestDto;
import com.innowise.orderservice.application.dto.OrderItemResponseDto;
import com.innowise.orderservice.domain.model.OrderItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemsMapper {

    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemPrice", source = "item.price")
    OrderItemResponseDto toDto(OrderItems entity);

    List<OrderItemResponseDto> toDtoList(List<OrderItems> entityList);
}
