package com.smartkitchen.repository;

import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByStatusOrderByIdAsc(OrderItemStatus status);

    long countByStatus(OrderItemStatus status);
}