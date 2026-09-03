package com.smartkitchen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartkitchen.dto.request.OrderItemRequest;
import com.smartkitchen.dto.request.PlaceOrderRequest;
import com.smartkitchen.dto.response.OrderResponse;
import com.smartkitchen.entity.MenuItem;
import com.smartkitchen.entity.Order;
import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.exception.InvalidOrderException;
import com.smartkitchen.repository.MenuItemRepository;
import com.smartkitchen.repository.OrderRepository;
import com.smartkitchen.service.order.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class TaskDependencyTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private MenuItemRepository menuRepository;

	@InjectMocks
	private OrderServiceImpl orderService;

	@Test
	void placeOrder_ValidDependencies_Success() {

		MenuItem m1 = MenuItem.builder().id(10L).name("Pizza Crust").cookTime(5).failureRate(0).build();
		MenuItem m2 = MenuItem.builder().id(20L).name("Toppings").cookTime(3).failureRate(0).build();

		when(menuRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(m1, m2));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order order = invocation.getArgument(0);
			order.setId(100L);
			long itemIdCounter = 1L;
			for (OrderItem item : order.getItems()) {
				item.setId(itemIdCounter++);
			}
			return order;
		});

		PlaceOrderRequest request = PlaceOrderRequest.builder()
				.customerName("Alice")
				.items(List.of(
						OrderItemRequest.builder().menuItemId(10L).dependsOnIndex(null).build(),
						OrderItemRequest.builder().menuItemId(20L).dependsOnIndex(0).build()
				))
				.build();

		OrderResponse response = orderService.placeOrder(request);

		assertNotNull(response);
		assertEquals(2, response.getItems().size());
		assertEquals("Pizza Crust", response.getItems().get(0).getItemName());
		assertEquals("Toppings", response.getItems().get(1).getItemName());
		assertEquals(1L, response.getItems().get(1).getDependsOnItemId());
	}

	@Test
	void placeOrder_CircularDependency_ThrowsException() {

		PlaceOrderRequest request = PlaceOrderRequest.builder()
				.customerName("Bob")
				.items(List.of(
						OrderItemRequest.builder().menuItemId(10L).dependsOnIndex(1).build(),
						OrderItemRequest.builder().menuItemId(20L).dependsOnIndex(0).build()
				))
				.build();

		InvalidOrderException exception = assertThrows(InvalidOrderException.class,
				() -> orderService.placeOrder(request));

		assertEquals("Circular dependency detected in order tasks", exception.getMessage());
	}

}
