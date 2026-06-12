package com.innowise.orderservice.application.service.impl;

import com.innowise.orderservice.application.dto.*;
import com.innowise.orderservice.application.mapper.OrderMapper;
import com.innowise.orderservice.application.service.OrderApplicationService;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.domain.model.Item;
import com.innowise.orderservice.domain.model.Order;
import com.innowise.orderservice.domain.model.OrderItems;
import com.innowise.orderservice.domain.port.out.ItemRepository;
import com.innowise.orderservice.domain.port.out.OrderRepository;
import com.innowise.orderservice.domain.port.out.UserServiceClient;
import com.innowise.orderservice.infrastructure.persistence.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;

    @Override
    //Not transactional
    public FullOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequest) throws ItemNotFoundException {

        Order order = new Order();
        order.setUserId(userId);

        for(OrderItemRequestDto itemRequestDto : createOrderRequest.items()){
            Item item = itemRepository.findById(itemRequestDto.itemId()).orElseThrow(() -> new ItemNotFoundException(itemRequestDto.itemId()));

            OrderItems orderItems = new OrderItems();

            orderItems.setOrder(order);
            orderItems.setItem(item);
            orderItems.setQuantity(itemRequestDto.quantity());
            order.addOrderItems(orderItems);
        }

        orderRepository.save(order);

        UserInfoResponseDto userInfoResponse = userServiceClient.geUserByUserId(userId);

        return new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order));
    }

    @Override
    public FullOrderResponseDto getOrderById(Long userId, Long orderId) throws OrderNotFoundException {

        Order order = orderRepository.findByIdWithDeletedFalse(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(!userId.equals(order.getUserId()))
            throw new AccessDeniedException("Cannot modify an order that doesn't belong to you");

        UserInfoResponseDto userInfoResponse = userServiceClient.geUserByUserId(userId);

        return new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order));
    }

    @Override
    public Page<FullOrderResponseDto> getOrderFilteredAndPaged(Long userId, OrderFilter filter, Pageable pageable) {


        Specification<Order> orderSpecification =
                Specification.allOf(
                        OrderSpecification.fromFilter(filter),
                        OrderSpecification.withUserId(userId)
                );

        UserInfoResponseDto userInfoResponse = userServiceClient.geUserByUserId(userId);

        return orderRepository.findAll(orderSpecification, pageable)
                .map(order -> new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order)));
    }

    @Override
    public List<FullOrderResponseDto> getOrdersByUserId(Long userId, Long userIdPathParamValue) {

        if(!userId.equals(userIdPathParamValue))
            throw new AccessDeniedException("Cannot read orders that don't belong to you");

        UserInfoResponseDto userInfoResponse = userServiceClient.geUserByUserId(userId);

        return orderRepository.findByUserIdWithDeletedFalse(userId).stream()
                .map(order -> new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order))).toList();
    }

    @Override
    @Transactional
    public FullOrderResponseDto updateOrderById(Long userId, Long orderId, UpdateOrderRequestDto updateOrderRequest)
            throws OrderNotFoundException {

        Order order = orderRepository.findByIdWithDeletedFalse(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(!userId.equals(order.getUserId()))
            throw new AccessDeniedException("Cannot modify an order that doesn't belong to you");

        orderMapper.updateEntity(updateOrderRequest, order);

        UserInfoResponseDto userInfoResponse = userServiceClient.geUserByUserId(userId);

        return new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order));
    }

    @Override
    public void deleteOrderById(Long userId, Long orderId) throws OrderNotFoundException {

        Order order = orderRepository.findByIdWithDeletedFalse(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(!userId.equals(order.getUserId()))
            throw new AccessDeniedException("Cannot modify an order that doesn't belong to you");

        orderRepository.deleteById(orderId);
    }
}
