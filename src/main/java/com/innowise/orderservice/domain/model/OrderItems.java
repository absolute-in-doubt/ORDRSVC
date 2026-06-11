package com.innowise.orderservice.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "order_items")
@Entity
@Data
@NoArgsConstructor
public class OrderItems {

    @SequenceGenerator(name = "order_items_gen", sequenceName = "order_items_seq")

    @Id
    @GeneratedValue(generator = "order_items_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name="item_id", referencedColumnName = "id")
    private Item item;

    @Column(name="quantity")
    private Long quantity;
}