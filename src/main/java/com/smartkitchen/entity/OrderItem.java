package com.smartkitchen.entity;

import java.time.LocalDateTime;

import com.smartkitchen.enums.OrderItemStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "order_id")
	private Order order;

	@ManyToOne
	@JoinColumn(name = "menu_item_id")
	private MenuItem menuItem;

	@ManyToOne
	@JoinColumn(name = "chef_id")
	private Chef chef;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "depends_on_item_id")
	private OrderItem dependsOnItem;

	@Enumerated(EnumType.STRING)
	private OrderItemStatus status;

	@Builder.Default
	private Integer attempts = 0;

	private LocalDateTime startedAt;

	private LocalDateTime completedAt;
}