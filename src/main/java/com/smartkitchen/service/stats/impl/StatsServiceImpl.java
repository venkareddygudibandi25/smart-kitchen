package com.smartkitchen.service.stats.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartkitchen.dto.response.StatsResponse;
import com.smartkitchen.enums.OrderItemStatus;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.OrderItemRepository;
import com.smartkitchen.service.stats.StatsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

	private final OrderItemRepository orderItemRepository;
	private final ChefRepository chefRepository;

	@Override
	@Transactional(readOnly = true)
	public StatsResponse getStats() {

		long runningTasks = orderItemRepository.countByStatus(OrderItemStatus.RUNNING);
		long waitingTasks = orderItemRepository.countByStatus(OrderItemStatus.WAITING);
		long availableChefs = chefRepository.countByAvailable(true);
		long busyChefs = chefRepository.countByAvailable(false);

		return StatsResponse.builder()
				.runningTasks(runningTasks)
				.waitingTasks(waitingTasks)
				.availableChefs(availableChefs)
				.busyChefs(busyChefs)
				.build();
	}
}
