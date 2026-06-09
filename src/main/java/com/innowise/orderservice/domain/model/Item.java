package com.innowise.orderservice.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "items")
@Entity
@EntityListeners({AuditingEntityListener.class})
@Data
public class Item {

    @SequenceGenerator(name = "items_gen", sequenceName = "items_seq")

    @Id
    @GeneratedValue(generator = "items_gen")
    private Long id;

    private String name;

    private BigDecimal price;


    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}