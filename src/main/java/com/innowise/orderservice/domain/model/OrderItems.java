package com.innowise.orderservice.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "order_items")
@Entity
@Data
public class OrderItems {

    @SequenceGenerator(name = "order_items_gen", sequenceName = "order_items_seq")

    @Id
    @GeneratedValue(generator = "order_items_gen")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name="quantity")
    private Long quantity;
}