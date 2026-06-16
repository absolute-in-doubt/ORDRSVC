package com.innowise.orderservice.application.service.impl;

import com.innowise.orderservice.application.dto.*;
import com.innowise.orderservice.application.mapper.OrderMapper;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.domain.model.*;
import com.innowise.orderservice.domain.port.out.ItemRepository;
import com.innowise.orderservice.domain.port.out.OrderRepository;
import com.innowise.orderservice.domain.port.out.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceImplUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderApplicationServiceImpl orderApplicationService;

    private Long userId = 1L;
    private Long orderId = 100L;
    private Long itemId = 200L;
    private Order order;
    private Item item;
    private UserInfoResponseDto userInfoResponseDto;
    private OrderResponseDto orderResponseDto;
    private CreateOrderRequestDto createOrderRequestDto;
    private UpdateOrderRequestDto updateOrderRequestDto;

    @BeforeEach
    void setUp() {
        userInfoResponseDto = new UserInfoResponseDto(1L, "John", "Doe", "john@example.com", true);

        item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setPrice(BigDecimal.valueOf(100.0));

        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);

        orderResponseDto = new OrderResponseDto(
                orderId,
                userId,
                OrderStatus.CREATED,
                BigDecimal.valueOf(100.0),
                List.of()
        );

        OrderItemRequestDto orderItemRequestDto = new OrderItemRequestDto(itemId, 2L);
        createOrderRequestDto = new CreateOrderRequestDto(List.of(orderItemRequestDto));
        updateOrderRequestDto = new UpdateOrderRequestDto(List.of(orderItemRequestDto));
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws ItemNotFoundException {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userServiceClient.getUserByUserId(userId)).thenReturn(userInfoResponseDto);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDto);

        FullOrderResponseDto result = orderApplicationService.createOrder(userId, createOrderRequestDto);

        assertNotNull(result);
        assertEquals(userInfoResponseDto, result.userInfo());
        assertEquals(orderResponseDto, result.order());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(itemRepository, times(1)).findById(itemId);
        verify(userServiceClient, times(1)).getUserByUserId(userId);
    }

    @Test
    void createOrder_shouldThrowItemNotFoundException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () ->
            orderApplicationService.createOrder(userId, createOrderRequestDto));
        verify(itemRepository, times(1)).findById(itemId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_shouldReturnOrderSuccessfully() throws OrderNotFoundException {
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserByUserId(userId)).thenReturn(userInfoResponseDto);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        FullOrderResponseDto result = orderApplicationService.getOrderById(userId, orderId);

        assertNotNull(result);
        assertEquals(userInfoResponseDto, result.userInfo());
        assertEquals(orderResponseDto, result.order());
        verify(orderRepository, times(1)).findByIdWithDeletedFalse(orderId);
    }

    @Test
    void getOrderById_shouldThrowOrderNotFoundException() {
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
            orderApplicationService.getOrderById(userId, orderId));
    }

    @Test
    void getOrderById_shouldThrowAccessDeniedException() {
        Long differentUserId = 2L;
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.getOrderById(differentUserId, orderId));
    }

    @Test
    void getOrderFilteredAndPaged_shouldReturnPagedOrders() {
        OrderFilter filter = new OrderFilter(LocalDateTime.now(), List.of(OrderStatus.CREATED));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAll(any(Specification.class), eq(pageable), any(String.class))).thenReturn(orderPage);
        when(userServiceClient.getUserByUserId(userId)).thenReturn(userInfoResponseDto);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        Page<FullOrderResponseDto> result = orderApplicationService.getOrderFilteredAndPaged(userId, filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(userInfoResponseDto, result.getContent().get(0).userInfo());
    }

    @Test
    void getOrdersByUserId_shouldReturnOrders() {
        when(userServiceClient.getUserByUserId(userId)).thenReturn(userInfoResponseDto);
        when(orderRepository.findByUserIdWithDeletedFalse(userId)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        List<FullOrderResponseDto> result = orderApplicationService.getOrdersByUserId(userId, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userInfoResponseDto, result.get(0).userInfo());
    }

    @Test
    void getOrdersByUserId_shouldThrowAccessDeniedException() {
        Long differentUserId = 2L;

        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.getOrdersByUserId(userId, differentUserId));
    }

    @Test
    void updateOrderById_shouldUpdateOrderSuccessfully() throws OrderNotFoundException, ItemNotFoundException {
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserByUserId(userId)).thenReturn(userInfoResponseDto);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        when(itemRepository.findById(eq(itemId))).thenReturn(Optional.of(item));

        FullOrderResponseDto result = orderApplicationService.updateOrderById(userId, orderId, updateOrderRequestDto);

        assertNotNull(result);
        assertEquals(userInfoResponseDto, result.userInfo());
        assertEquals(orderResponseDto, result.order());
    }

    @Test
    void updateOrderById_shouldThrowOrderNotFoundException() {
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
            orderApplicationService.updateOrderById(userId, orderId, updateOrderRequestDto));
    }

    @Test
    void updateOrderById_shouldThrowAccessDeniedException() {
        Long differentUserId = 2L;
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.updateOrderById(differentUserId, orderId, updateOrderRequestDto));
    }

    @Test
    void deleteOrderById_shouldDeleteOrderSuccessfully() throws OrderNotFoundException {
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.of(order));

        orderApplicationService.deleteOrderById(userId, orderId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void deleteOrderById_shouldThrowOrderNotFoundException() {
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
            orderApplicationService.deleteOrderById(userId, orderId));
    }

    @Test
    void deleteOrderById_shouldThrowAccessDeniedException() {
        Long differentUserId = 2L;
        when(orderRepository.findByIdWithDeletedFalse(orderId)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.deleteOrderById(differentUserId, orderId));
    }
}