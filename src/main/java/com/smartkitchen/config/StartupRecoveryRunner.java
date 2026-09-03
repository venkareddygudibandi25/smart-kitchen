package com.smartkitchen.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartkitchen.entity.Chef;
import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.enums.OrderItemStatus;
import com.smartkitchen.enums.OrderStatus;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.OrderItemRepository;
import com.smartkitchen.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Startup recovery runner that resets tasks interrupted by a server shutdown/restart.
 * Requeues RUNNING tasks back to WAITING status and releases assigned chefs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupRecoveryRunner implements CommandLineRunner {

	private final OrderItemRepository orderItemRepository;
	private final ChefRepository chefRepository;
	private final OrderRepository orderRepository;

	@Override
	@Transactional
	public void run(String... args) {

		List<OrderItem> runningItems = orderItemRepository.findByStatusOrderByIdAsc(OrderItemStatus.RUNNING);

		if (!runningItems.isEmpty()) {
			log.info("Found {} RUNNING tasks interrupted by system restart. Resetting to WAITING...", runningItems.size());

			for (OrderItem item : runningItems) {
				item.setStatus(OrderItemStatus.WAITING);
				item.setChef(null);
				item.setStartedAt(null);
				orderItemRepository.save(item);

				if (item.getOrder() != null && item.getOrder().getStatus() == OrderStatus.RUNNING) {
					item.getOrder().setStatus(OrderStatus.WAITING);
					orderRepository.save(item.getOrder());
				}
			}
		}

		List<Chef> chefs = chefRepository.findAll();
		for (Chef chef : chefs) {
			if (!chef.getAvailable()) {
				chef.setAvailable(true);
				chefRepository.save(chef);
			}
		}

		log.info("Startup recovery complete. All chefs marked available.");
	}
}
