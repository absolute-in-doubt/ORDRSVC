package com.innowise.orderservice.application.service.impl;

import com.innowise.orderservice.application.dto.*;
import com.innowise.orderservice.application.mapper.OrderMapper;
import com.innowise.orderservice.application.service.OrderService;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.model.Item;
import com.innowise.orderservice.domain.model.Order;
import com.innowise.orderservice.domain.model.OrderItems;
import com.innowise.orderservice.domain.port.out.ItemRepository;
import com.innowise.orderservice.domain.port.out.OrderRepository;
import com.innowise.orderservice.domain.port.out.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

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
    public FullOrderResponseDto getOrderById(Long orderId) {
        return null;
    }

    @Override
    public Page<FullOrderResponseDto> getOrderFilteredAndPaged(OrderFilter filter, Pageable pageable) {
        return null;
    }

    @Override
    public List<FullOrderResponseDto> getOrdersByUserId(Long userId) {
        return List.of();
    }

    @Override
    public FullOrderResponseDto updateOrderById(Long orderId, UpdateOrderRequestDto updateOrderRequest) {
        return null;
    }

    @Override
    public void deleteOrderById(Long orderId) {

    }
}
