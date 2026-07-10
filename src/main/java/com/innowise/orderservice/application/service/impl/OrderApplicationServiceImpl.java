package com.innowise.orderservice.application.service.impl;

import com.innowise.orderservice.application.dto.*;
import com.innowise.orderservice.application.mapper.OrderMapper;
import com.innowise.orderservice.application.service.OrderApplicationService;
import com.innowise.orderservice.application.specification.OrderSpecification;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.domain.model.Item;
import com.innowise.orderservice.domain.model.Order;
import com.innowise.orderservice.domain.model.OrderItems;
import com.innowise.orderservice.domain.model.OrderStatus;
import com.innowise.orderservice.domain.port.out.ItemRepository;
import com.innowise.orderservice.domain.port.out.OrderRepository;
import com.innowise.orderservice.domain.port.out.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;

    @Override
    /**
     * Non Transactional
     * Because: the first part are just queries that don't require any cascading or lazy loading.
     * If anything fails, it fails before the save().
     * If item gets deleted after it's read, save() just fails (may add @Retryable).
     * So adding @Transactional would just slow down the method a bit and add additional pressure on the DB.
     */
    public FullOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequest) throws ItemNotFoundException {

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);

        for(OrderItemRequestDto itemRequestDto : createOrderRequest.items()){
            Item item = itemRepository.findById(itemRequestDto.itemId()).orElseThrow(() -> new ItemNotFoundException(itemRequestDto.itemId()));

            OrderItems orderItems = new OrderItems();

            orderItems.setOrder(order);
            orderItems.setItem(item);
            orderItems.setQuantity(itemRequestDto.quantity());
            order.addOrderItems(orderItems);
        }

        UserInfoResponseDto userInfoResponse = userServiceClient.getUserByUserId(userId);

        orderRepository.save(order);

        return new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order));
    }

    @Override
    public FullOrderResponseDto getOrderById(Long userId, Long orderId) throws OrderNotFoundException {

        Order order = orderRepository.findByIdWithDeletedFalse(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(!userId.equals(order.getUserId()))
            throw new AccessDeniedException("Cannot modify an order that doesn't belong to you");

        UserInfoResponseDto userInfoResponse = userServiceClient.getUserByUserId(userId);

        return new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order));
    }

    @Override
    public Page<FullOrderResponseDto> getOrderFilteredAndPaged(Long userId, OrderFilter filter, Pageable pageable) {


        Specification<Order> orderSpecification =
                Specification.allOf(
                        OrderSpecification.fromFilter(filter),
                        OrderSpecification.withUserId(userId)
                );

        UserInfoResponseDto userInfoResponse = userServiceClient.getUserByUserId(userId);

        return orderRepository.findAll(orderSpecification, pageable, "order-with-items")
                .map(order -> new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order)));
    }

    @Override
    public List<FullOrderResponseDto> getOrdersByUserId(Long userId, Long userIdPathParamValue) {

        if(!userId.equals(userIdPathParamValue))
            throw new AccessDeniedException("Cannot read orders that don't belong to you");

        UserInfoResponseDto userInfoResponse = userServiceClient.getUserByUserId(userId);

        return orderRepository.findByUserIdWithDeletedFalse(userId).stream()
                .map(order -> new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order))).toList();
    }

    @Override
    @Transactional
    public FullOrderResponseDto updateOrderById(Long userId, Long orderId, UpdateOrderRequestDto updateOrderRequest)
            throws OrderNotFoundException, ItemNotFoundException {

        Order order = orderRepository.findByIdWithDeletedFalse(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(!userId.equals(order.getUserId()))
            throw new AccessDeniedException("Cannot modify an order that doesn't belong to you");


        log.trace("Initial order; {}", order);

        order.getItems().clear();
        for(OrderItemRequestDto itemRequestDto : updateOrderRequest.items()){
            Item item = itemRepository.findById(itemRequestDto.itemId()).orElseThrow(() -> new ItemNotFoundException(itemRequestDto.itemId()));

            OrderItems orderItems = new OrderItems();

            orderItems.setOrder(order);
            orderItems.setItem(item);
            orderItems.setQuantity(itemRequestDto.quantity());
            order.addOrderItems(orderItems);
        }

        log.trace("Updated order; {}", order);

        UserInfoResponseDto userInfoResponse = userServiceClient.getUserByUserId(userId);

        return new FullOrderResponseDto(userInfoResponse, orderMapper.toDto(order));
    }

    @Override
    @Transactional
    public void deleteOrderById(Long userId, Long orderId) throws OrderNotFoundException {

        Order order = orderRepository.findByIdWithDeletedFalse(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if(!userId.equals(order.getUserId()))
            throw new AccessDeniedException("Cannot modify an order that doesn't belong to you");

        orderRepository.deleteById(orderId);
    }

    @Override
    @Transactional
    public void updateOrderStatus(UpdateOrderStatusRequestDto requestDto) throws OrderNotFoundException {

        Order order = orderRepository.findByIdWithDeletedFalse(requestDto.orderId())
                .orElseThrow(() -> new OrderNotFoundException(requestDto.orderId()));
        order.setStatus(requestDto.orderStatus());
    }
}
