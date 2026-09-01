package com.smartkitchen.service.order.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartkitchen.dto.request.PlaceOrderRequest;
import com.smartkitchen.dto.response.OrderItemResponse;
import com.smartkitchen.dto.response.OrderResponse;
import com.smartkitchen.entity.MenuItem;
import com.smartkitchen.entity.Order;
import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.enums.OrderItemStatus;
import com.smartkitchen.enums.OrderStatus;
import com.smartkitchen.exception.InvalidOrderException;
import com.smartkitchen.exception.OrderNotFoundException;
import com.smartkitchen.repository.MenuItemRepository;
import com.smartkitchen.repository.OrderRepository;
import com.smartkitchen.service.order.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	private final OrderRepository orderRepository;
	private final MenuItemRepository menuRepository;

	@Override
	@Transactional
	public OrderResponse placeOrder(PlaceOrderRequest request) {

		List<MenuItem> menuItems = menuRepository.findAllById(request.getItemIds());

		if (menuItems.size() != request.getItemIds().size()) {
			throw new InvalidOrderException("One or more menu items do not exist");
		}

		Order order = Order.builder().customerName(request.getCustomerName()).status(OrderStatus.WAITING)
				.createdAt(LocalDateTime.now()).build();

		List<OrderItem> orderItems = menuItems.stream().map(item -> OrderItem.builder().order(order).menuItem(item)
				.status(OrderItemStatus.WAITING).attempts(0).build()).toList();

		order.setItems(orderItems);

		Order saved = orderRepository.save(order);

		return mapToResponse(saved);
	}

	@Override
	public OrderResponse getOrder(Long id) {

		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

		return mapToResponse(order);
	}

	@Override
	@Transactional
	public void cancelOrder(Long id) {

		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

		order.setStatus(OrderStatus.CANCELLED);

		order.getItems().forEach(item -> {

			if (item.getStatus() == OrderItemStatus.WAITING) {

				item.setStatus(OrderItemStatus.CANCELLED);
			}
		});
	}

	private OrderResponse mapToResponse(Order order) {

		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> OrderItemResponse.builder().itemId(item.getId()).itemName(item.getMenuItem().getName())
						.chefName(item.getChef() == null ? null : item.getChef().getName())
						.status(item.getStatus().name()).attempts(item.getAttempts()).build())
				.toList();

		return OrderResponse.builder().orderId(order.getId()).customerName(order.getCustomerName())
				.status(order.getStatus().name()).items(items).build();
	}

}
