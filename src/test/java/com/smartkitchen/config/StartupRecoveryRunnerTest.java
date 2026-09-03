package com.smartkitchen.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartkitchen.entity.Chef;
import com.smartkitchen.entity.Order;
import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.enums.OrderItemStatus;
import com.smartkitchen.enums.OrderStatus;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.OrderItemRepository;
import com.smartkitchen.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class StartupRecoveryRunnerTest {

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private ChefRepository chefRepository;

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private StartupRecoveryRunner recoveryRunner;

	@Test
	void run_RecoversInterruptedRunningTasksAndReleasesChefs() {

		Order order = Order.builder().id(1L).status(OrderStatus.RUNNING).build();
		Chef chef = Chef.builder().id(10L).name("Chef John").available(false).build();
		OrderItem item = OrderItem.builder()
				.id(100L)
				.order(order)
				.chef(chef)
				.status(OrderItemStatus.RUNNING)
				.build();

		when(orderItemRepository.findByStatusOrderByIdAsc(OrderItemStatus.RUNNING)).thenReturn(List.of(item));
		when(chefRepository.findAll()).thenReturn(List.of(chef));

		recoveryRunner.run();

		assertEquals(OrderItemStatus.WAITING, item.getStatus());
		assertNull(item.getChef());
		assertEquals(OrderStatus.WAITING, order.getStatus());
		assertTrue(chef.getAvailable());

		verify(orderItemRepository).save(item);
		verify(orderRepository).save(order);
		verify(chefRepository).save(chef);
	}
}
