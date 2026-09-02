package com.smartkitchen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.enums.OrderItemStatus;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByStatusOrderByIdAsc(OrderItemStatus status);

	List<OrderItem> findByOrderId(Long orderId);

	long countByStatus(OrderItemStatus status);
}