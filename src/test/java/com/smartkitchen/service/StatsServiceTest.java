package com.smartkitchen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartkitchen.dto.response.StatsResponse;
import com.smartkitchen.enums.OrderItemStatus;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.OrderItemRepository;
import com.smartkitchen.service.stats.impl.StatsServiceImpl;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private ChefRepository chefRepository;

	@InjectMocks
	private StatsServiceImpl statsService;

	@Test
	void getStats_ReturnsCorrectCounts() {

		when(orderItemRepository.countByStatus(OrderItemStatus.RUNNING)).thenReturn(2L);
		when(orderItemRepository.countByStatus(OrderItemStatus.WAITING)).thenReturn(5L);
		when(chefRepository.countByAvailable(true)).thenReturn(3L);
		when(chefRepository.countByAvailable(false)).thenReturn(2L);

		StatsResponse stats = statsService.getStats();

		assertNotNull(stats);
		assertEquals(2L, stats.getRunningTasks());
		assertEquals(5L, stats.getWaitingTasks());
		assertEquals(3L, stats.getAvailableChefs());
		assertEquals(2L, stats.getBusyChefs());
	}
}
