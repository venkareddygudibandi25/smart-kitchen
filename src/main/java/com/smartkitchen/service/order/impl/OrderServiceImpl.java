package com.smartkitchen.service.order.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartkitchen.dto.request.OrderItemRequest;
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

		List<OrderItemRequest> itemRequests = resolveItemRequests(request);

		if (itemRequests == null || itemRequests.isEmpty()) {
			throw new InvalidOrderException("Order must contain at least one menu item");
		}

		validateCircularDependencies(itemRequests);

		List<Long> menuItemIds = itemRequests.stream().map(OrderItemRequest::getMenuItemId).toList();
		List<MenuItem> menuItems = menuRepository.findAllById(menuItemIds);

		if (menuItems.size() != menuItemIds.size()) {
			throw new InvalidOrderException("One or more menu items do not exist");
		}

		Order order = Order.builder()
				.customerName(request.getCustomerName())
				.status(OrderStatus.WAITING)
				.createdAt(LocalDateTime.now())
				.build();

		List<OrderItem> orderItems = new ArrayList<>();
		for (int i = 0; i < itemRequests.size(); i++) {
			Long mId = itemRequests.get(i).getMenuItemId();
			MenuItem menuItem = menuItems.stream().filter(m -> m.getId().equals(mId)).findFirst().orElseThrow();

			OrderItem item = OrderItem.builder()
					.order(order)
					.menuItem(menuItem)
					.status(OrderItemStatus.WAITING)
					.attempts(0)
					.build();
			orderItems.add(item);
		}

		for (int i = 0; i < itemRequests.size(); i++) {
			Integer depIndex = itemRequests.get(i).getDependsOnIndex();
			if (depIndex != null) {
				orderItems.get(i).setDependsOnItem(orderItems.get(depIndex));
			}
		}

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
			if (item.getStatus() == OrderItemStatus.WAITING || item.getStatus() == OrderItemStatus.RUNNING) {
				item.setStatus(OrderItemStatus.CANCELLED);
			}
			if (item.getChef() != null) {
				item.getChef().setAvailable(true);
				item.setChef(null);
			}
		});
	}

	private List<OrderItemRequest> resolveItemRequests(PlaceOrderRequest request) {

		if (request.getItems() != null && !request.getItems().isEmpty()) {
			return request.getItems();
		}
		if (request.getItemIds() != null && !request.getItemIds().isEmpty()) {
			return request.getItemIds().stream()
					.map(id -> OrderItemRequest.builder().menuItemId(id).build())
					.toList();
		}
		return List.of();
	}

	private void validateCircularDependencies(List<OrderItemRequest> itemRequests) {

		int n = itemRequests.size();
		int[] state = new int[n];

		for (int i = 0; i < n; i++) {
			if (state[i] == 0) {
				dfsCheckCycle(i, itemRequests, state);
			}
		}
	}

	private void dfsCheckCycle(int u, List<OrderItemRequest> itemRequests, int[] state) {

		state[u] = 1;

		Integer parentIndex = itemRequests.get(u).getDependsOnIndex();
		if (parentIndex != null) {
			if (parentIndex < 0 || parentIndex >= itemRequests.size() || parentIndex == u) {
				throw new InvalidOrderException("Invalid dependency index: " + parentIndex);
			}

			if (state[parentIndex] == 1) {
				throw new InvalidOrderException("Circular dependency detected in order tasks");
			}

			if (state[parentIndex] == 0) {
				dfsCheckCycle(parentIndex, itemRequests, state);
			}
		}

		state[u] = 2;
	}

	private OrderResponse mapToResponse(Order order) {

		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> OrderItemResponse.builder()
						.itemId(item.getId())
						.itemName(item.getMenuItem().getName())
						.chefName(item.getChef() == null ? null : item.getChef().getName())
						.status(item.getStatus().name())
						.attempts(item.getAttempts())
						.dependsOnItemId(item.getDependsOnItem() == null ? null : item.getDependsOnItem().getId())
						.build())
				.toList();

		return OrderResponse.builder()
				.orderId(order.getId())
				.customerName(order.getCustomerName())
				.status(order.getStatus().name())
				.items(items)
				.build();
	}

}
