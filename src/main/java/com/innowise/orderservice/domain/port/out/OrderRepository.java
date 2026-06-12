package com.innowise.orderservice.domain.port.out;

import com.innowise.orderservice.domain.model.Order;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends
        JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order>,
        CustomOrderRepository {

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.deleted = true WHERE o.id = :orderId")
    void deleteById(@Param("orderId") @NotNull Long id);


    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId AND o.deleted = false")
    Optional<Order> findByIdWithDeletedFalse(@Param("orderId") @NotNull Long id);


    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.userId = :userId AND o.deleted = false")
    List<Order> findByUserIdWithDeletedFalse(@Param("userId") @NotNull Long userId);
}