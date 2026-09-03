package com.smartkitchen.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.smartkitchen.entity.Chef;
import com.smartkitchen.entity.Order;
import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.enums.OrderItemStatus;
import com.smartkitchen.enums.OrderStatus;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.OrderItemRepository;
import com.smartkitchen.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class RetryAndFailureTest {

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private ChefRepository chefRepository;

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private SmartKitchenScheduler scheduler;

	@Test
	void completeTask_FirstFailure_RequeuesTaskAsWaitingAndIncrementsAttempts() {

		ReflectionTestUtils.setField(scheduler, "maxRetries", 3);

		Chef chef = Chef.builder().id(1L).name("Chef Ram").available(false).build();
		Order order = Order.builder().id(10L).status(OrderStatus.RUNNING).build();
		OrderItem item = OrderItem.builder()
				.id(100L)
				.order(order)
				.chef(chef)
				.status(OrderItemStatus.RUNNING)
				.attempts(0)
				.build();

		when(orderItemRepository.findById(100L)).thenReturn(java.util.Optional.of(item));
		when(chefRepository.findById(1L)).thenReturn(java.util.Optional.of(chef));
		when(orderRepository.findById(10L)).thenReturn(java.util.Optional.of(order));
		when(orderItemRepository.findByOrderId(10L)).thenReturn(List.of(item));

		scheduler.completeTask(100L, 1L, false);

		assertEquals(1, item.getAttempts());
		assertEquals(OrderItemStatus.WAITING, item.getStatus());
		assertEquals(true, chef.getAvailable());
	}

	@Test
	void completeTask_ExceedMaxRetries_MarksTaskAsFailed() {

		ReflectionTestUtils.setField(scheduler, "maxRetries", 3);

		Chef chef = Chef.builder().id(1L).name("Chef Ram").available(false).build();
		Order order = Order.builder().id(10L).status(OrderStatus.RUNNING).build();
		OrderItem item = OrderItem.builder()
				.id(100L)
				.order(order)
				.chef(chef)
				.status(OrderItemStatus.RUNNING)
				.attempts(2)
				.build();

		when(orderItemRepository.findById(100L)).thenReturn(java.util.Optional.of(item));
		when(chefRepository.findById(1L)).thenReturn(java.util.Optional.of(chef));
		when(orderRepository.findById(10L)).thenReturn(java.util.Optional.of(order));
		when(orderItemRepository.findByOrderId(10L)).thenReturn(List.of(item));

		scheduler.completeTask(100L, 1L, false);

		assertEquals(3, item.getAttempts());
		assertEquals(OrderItemStatus.FAILED, item.getStatus());
		assertEquals(OrderStatus.FAILED, order.getStatus());
		assertEquals(true, chef.getAvailable());
	}

	@Test
	void processBlockedTasks_ParentFailed_MarksChildBlocked() {

		Order order = Order.builder().id(10L).status(OrderStatus.RUNNING).build();

		OrderItem parentItem = OrderItem.builder()
				.id(100L)
				.order(order)
				.status(OrderItemStatus.FAILED)
				.build();

		OrderItem childItem = OrderItem.builder()
				.id(101L)
				.order(order)
				.status(OrderItemStatus.WAITING)
				.dependsOnItem(parentItem)
				.build();

		when(orderItemRepository.findByStatusOrderByIdAsc(OrderItemStatus.WAITING)).thenReturn(List.of(childItem));
		when(orderRepository.findById(10L)).thenReturn(java.util.Optional.of(order));
		when(orderItemRepository.findByOrderId(10L)).thenReturn(List.of(parentItem, childItem));

		scheduler.processBlockedTasks();

		assertEquals(OrderItemStatus.BLOCKED, childItem.getStatus());
		verify(orderItemRepository).save(childItem);
	}
}
