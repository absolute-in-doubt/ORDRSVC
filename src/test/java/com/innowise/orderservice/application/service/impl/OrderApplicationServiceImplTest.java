package com.innowise.orderservice.application.service.impl;

import com.innowise.orderservice.TestcontainersConfiguration;
import com.innowise.orderservice.application.dto.*;
import com.innowise.orderservice.application.mapper.OrderMapper;
import com.innowise.orderservice.domain.exception.ItemNotFoundException;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import com.innowise.orderservice.domain.model.*;
import com.innowise.orderservice.domain.port.out.ItemRepository;
import com.innowise.orderservice.domain.port.out.OrderRepository;
import com.innowise.orderservice.domain.port.out.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = TestcontainersConfiguration.class)
class OrderApplicationServiceImplTest {

    @Autowired
    private OrderApplicationServiceImpl orderApplicationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @Autowired
    private OrderMapper orderMapper;

    private Long userId = 1L;
    private Long itemId = 100L;
    private Item item;
    private UserInfoResponseDto userInfoResponseDto;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();

        userInfoResponseDto = new UserInfoResponseDto(1L, "John", "Doe", "john@example.com", true);
        when(userServiceClient.getUserByUserId(any(Long.class))).thenReturn(userInfoResponseDto);

        item = new Item();
        item.setName("Test Item");
        item.setPrice(BigDecimal.valueOf(50.0));
        item = itemRepository.save(item);
        itemId = item.getId();
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws ItemNotFoundException {
        OrderItemRequestDto orderItemRequestDto = new OrderItemRequestDto(itemId, 2L);
        CreateOrderRequestDto request = new CreateOrderRequestDto(List.of(orderItemRequestDto));

        FullOrderResponseDto result = orderApplicationService.createOrder(userId, request);

        assertNotNull(result);
        assertNotNull(result.order());
        assertEquals(userId, result.order().userId());
        assertEquals(OrderStatus.CREATED, result.order().status());
        assertNotNull(result.order().id());
        assertFalse(result.order().items().isEmpty());
        assertEquals(1, result.order().items().size());
        assertEquals(itemId, result.order().items().get(0).itemId());
        assertEquals(2L, result.order().items().get(0).quantity());
    }

    @Test
    void createOrder_shouldThrowItemNotFoundException() {
        OrderItemRequestDto orderItemRequestDto = new OrderItemRequestDto(9999L, 2L);
        CreateOrderRequestDto request = new CreateOrderRequestDto(List.of(orderItemRequestDto));

        assertThrows(ItemNotFoundException.class, () ->
            orderApplicationService.createOrder(userId, request));
    }

    @Test
    void getOrderById_shouldReturnOrderSuccessfully() throws OrderNotFoundException {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);
        order = orderRepository.save(order);
        Long orderId = order.getId();

        FullOrderResponseDto result = orderApplicationService.getOrderById(userId, orderId);

        assertNotNull(result);
        assertEquals(orderId, result.order().id());
        assertEquals(userId, result.order().userId());
    }

    @Test
    void getOrderById_shouldThrowOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class, () ->
            orderApplicationService.getOrderById(userId, 9999L));
    }

    @Test
    void getOrderById_shouldThrowAccessDeniedException() throws OrderNotFoundException {
        Order order = new Order();
        order.setUserId(2L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);
        order = orderRepository.save(order);
        Long orderId = order.getId();

        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.getOrderById(userId, orderId));
    }

    @Test
    void getOrderFilteredAndPaged_shouldReturnPagedOrders() {
        Order order1 = new Order();
        order1.setUserId(userId);
        order1.setStatus(OrderStatus.CREATED);
        order1.setTotalPrice(BigDecimal.valueOf(100.0));
        order1.setDeleted(false);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(userId);
        order2.setStatus(OrderStatus.PAID);
        order2.setTotalPrice(BigDecimal.valueOf(200.0));
        order2.setDeleted(false);
        orderRepository.save(order2);

        OrderFilter filter = new OrderFilter(null, null, List.of(OrderStatus.CREATED));
        Pageable pageable = PageRequest.of(0, 10);

        Page<FullOrderResponseDto> result = orderApplicationService.getOrderFilteredAndPaged(userId, filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(OrderStatus.CREATED, result.getContent().get(0).order().status());
    }

    @Test
    void getOrdersByUserId_shouldReturnOrders() {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);
        orderRepository.save(order);

        List<FullOrderResponseDto> result = orderApplicationService.getOrdersByUserId(userId, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).order().userId());
    }

    @Test
    void getOrdersByUserId_shouldThrowAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.getOrdersByUserId(userId, 2L));
    }

    @Test
    void updateOrderById_shouldUpdateOrderSuccessfully() throws OrderNotFoundException, ItemNotFoundException {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);
        order = orderRepository.save(order);
        Long orderId = order.getId();

        OrderItemRequestDto orderItemRequestDto = new OrderItemRequestDto(itemId, 3L);
        UpdateOrderRequestDto request = new UpdateOrderRequestDto(List.of(orderItemRequestDto));

        log.trace("Update request: {}", request);

        FullOrderResponseDto result = orderApplicationService.updateOrderById(userId, orderId, request);

        assertNotNull(result);
        assertEquals(orderId, result.order().id());
    }

    @Test
    void updateOrderById_shouldThrowOrderNotFoundException() {
        UpdateOrderRequestDto request = new UpdateOrderRequestDto(List.of());

        assertThrows(OrderNotFoundException.class, () ->
            orderApplicationService.updateOrderById(userId, 9999L, request));
    }

    @Test
    void updateOrderById_shouldThrowAccessDeniedException() {
        Order order = new Order();
        order.setUserId(2L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);
        order = orderRepository.save(order);
        Long orderId = order.getId();

        UpdateOrderRequestDto request = new UpdateOrderRequestDto(List.of());

        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.updateOrderById(userId, orderId, request));
    }

    @Test
    void deleteOrderById_shouldDeleteOrderSuccessfully() throws OrderNotFoundException {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);
        order = orderRepository.save(order);
        Long orderId = order.getId();

        orderApplicationService.deleteOrderById(userId, orderId);

        assertThrows(OrderNotFoundException.class, () ->
            orderApplicationService.getOrderById(userId, orderId));
    }

    @Test
    void deleteOrderById_shouldThrowOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class, () ->
            orderApplicationService.deleteOrderById(userId, 9999L));
    }

    @Test
    void deleteOrderById_shouldThrowAccessDeniedException() {
        Order order = new Order();
        order.setUserId(2L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setDeleted(false);
        order = orderRepository.save(order);
        Long orderId = order.getId();

        assertThrows(AccessDeniedException.class, () ->
            orderApplicationService.deleteOrderById(userId, orderId));
    }
}
