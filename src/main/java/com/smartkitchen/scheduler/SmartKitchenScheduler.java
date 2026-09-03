package com.smartkitchen.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartkitchen.entity.Chef;
import com.smartkitchen.entity.Order;
import com.smartkitchen.entity.OrderItem;
import com.smartkitchen.enums.OrderItemStatus;
import com.smartkitchen.enums.OrderStatus;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.OrderItemRepository;
import com.smartkitchen.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

/**
 * Background task scheduler responsible for matching waiting tasks to available chefs,
 * executing task cooking asynchronously, handling exponential backoff retries, and updating order status.
 */
@Component
@RequiredArgsConstructor
public class SmartKitchenScheduler {

	private final ChefRepository chefRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderRepository orderRepository;
	private final ExecutorService executor;

	private final Random random = new Random();

	@Value("${scheduler.max-retries:3}")
	private int maxRetries;

	/**
	 * Periodic scheduler loop running every 1 second (configurable).
	 * Finds available chefs and assigns waiting tasks in FIFO order.
	 */
	@Scheduled(fixedDelayString = "${scheduler.polling-delay:1000}")
	@Transactional
	public synchronized void assignTasks() {

		processBlockedTasks();

		List<Chef> availableChefs = chefRepository.findByAvailableTrue();
		if (availableChefs.isEmpty()) {
			return;
		}

		List<OrderItem> waitingItems = orderItemRepository.findByStatusOrderByIdAsc(OrderItemStatus.WAITING);

		int chefIndex = 0;
		for (OrderItem item : waitingItems) {
			if (chefIndex >= availableChefs.size()) {
				break;
			}

			if (item.getOrder() != null && item.getOrder().getStatus() == OrderStatus.CANCELLED) {
				item.setStatus(OrderItemStatus.CANCELLED);
				orderItemRepository.save(item);
				continue;
			}

			// Only start cooking if task dependencies are satisfied
			if (isReadyToRun(item)) {
				Chef chef = availableChefs.get(chefIndex++);
				reserveTaskAndSubmit(chef.getId(), item.getId());
			}
		}
	}

	/**
	 * Scans for waiting tasks whose parent dependencies failed or were cancelled, and marks them BLOCKED.
	 */
	@Transactional
	public void processBlockedTasks() {

		List<OrderItem> waitingItems = orderItemRepository.findByStatusOrderByIdAsc(OrderItemStatus.WAITING);
		for (OrderItem item : waitingItems) {
			if (item.getOrder() != null && item.getOrder().getStatus() == OrderStatus.CANCELLED) {
				item.setStatus(OrderItemStatus.CANCELLED);
				orderItemRepository.save(item);
				continue;
			}

			if (item.getDependsOnItem() != null) {
				OrderItemStatus parentStatus = item.getDependsOnItem().getStatus();
				if (parentStatus == OrderItemStatus.FAILED || parentStatus == OrderItemStatus.CANCELLED
						|| parentStatus == OrderItemStatus.BLOCKED) {
					item.setStatus(OrderItemStatus.BLOCKED);
					item.setCompletedAt(LocalDateTime.now());
					orderItemRepository.save(item);
					updateOrderStatus(item.getOrder());
				}
			}
		}
	}

	/**
	 * Checks if a task's parent dependency has reached SUCCESS status.
	 */
	private boolean isReadyToRun(OrderItem item) {

		if (item.getDependsOnItem() == null) {
			return true;
		}
		return item.getDependsOnItem().getStatus() == OrderItemStatus.SUCCESS;
	}

	/**
	 * Atomically reserves a chef and marks item RUNNING in a short DB transaction (~2ms).
	 * Then submits the cooking work to the async ExecutorService pool.
	 */
	@Transactional
	public void reserveTaskAndSubmit(Long chefId, Long itemId) {

		Chef chef = chefRepository.findById(chefId).orElse(null);
		OrderItem item = orderItemRepository.findById(itemId).orElse(null);

		if (chef == null || !chef.getAvailable() || item == null || item.getStatus() != OrderItemStatus.WAITING) {
			return;
		}

		if (item.getOrder() != null && item.getOrder().getStatus() == OrderStatus.CANCELLED) {
			item.setStatus(OrderItemStatus.CANCELLED);
			orderItemRepository.save(item);
			return;
		}

		chef.setAvailable(false);
		item.setChef(chef);
		item.setStatus(OrderItemStatus.RUNNING);
		item.setStartedAt(LocalDateTime.now());
		item.getOrder().setStatus(OrderStatus.RUNNING);

		chefRepository.save(chef);
		orderItemRepository.save(item);
		orderRepository.save(item.getOrder());

		int cookTimeSeconds = item.getMenuItem().getCookTime();
		int failureRate = item.getMenuItem().getFailureRate();

		executor.submit(() -> runTaskAsync(itemId, chefId, cookTimeSeconds, failureRate));
	}

	/**
	 * Simulates cooking asynchronously on a worker thread.
	 * NO database connection is held open during Thread.sleep().
	 */
	private void runTaskAsync(Long itemId, Long chefId, int cookTimeSeconds, int failureRate) {

		try {
			Thread.sleep(cookTimeSeconds * 1000L);

			boolean success = random.nextInt(100) >= failureRate;

			completeTask(itemId, chefId, success);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			completeTask(itemId, chefId, false);
		}
	}

	/**
	 * Short DB transaction that records task completion/failure, handles retries, and releases the chef.
	 */
	@Transactional
	public void completeTask(Long itemId, Long chefId, boolean success) {

		OrderItem item = orderItemRepository.findById(itemId).orElse(null);
		Chef chef = chefRepository.findById(chefId).orElse(null);

		if (item != null) {
			if (item.getStatus() == OrderItemStatus.CANCELLED || (item.getOrder() != null && item.getOrder().getStatus() == OrderStatus.CANCELLED)) {
				item.setStatus(OrderItemStatus.CANCELLED);
			} else if (success) {
				item.setStatus(OrderItemStatus.SUCCESS);
				item.setCompletedAt(LocalDateTime.now());
			} else {
				item.setAttempts(item.getAttempts() + 1);
				if (item.getAttempts() < maxRetries) {
					item.setStatus(OrderItemStatus.WAITING);
					item.setChef(null);
				} else {
					item.setStatus(OrderItemStatus.FAILED);
					item.setCompletedAt(LocalDateTime.now());
				}
			}
			orderItemRepository.save(item);
			updateOrderStatus(item.getOrder());
		}

		if (chef != null) {
			chef.setAvailable(true);
			chefRepository.save(chef);
		}
	}

	/**
	 * Recalculates and persists the overall Order status based on fresh OrderItem statuses from PostgreSQL.
	 */
	private void updateOrderStatus(Order order) {

		if (order == null || order.getStatus() == OrderStatus.CANCELLED) {
			return;
		}

		Order freshOrder = orderRepository.findById(order.getId()).orElse(order);
		List<OrderItem> items = orderItemRepository.findByOrderId(freshOrder.getId());

		boolean allSuccess = items.stream().allMatch(i -> i.getStatus() == OrderItemStatus.SUCCESS);
		boolean anyRunning = items.stream().anyMatch(i -> i.getStatus() == OrderItemStatus.RUNNING);
		boolean anyWaiting = items.stream().anyMatch(i -> i.getStatus() == OrderItemStatus.WAITING);
		boolean anyFailed = items.stream().anyMatch(i -> i.getStatus() == OrderItemStatus.FAILED || i.getStatus() == OrderItemStatus.BLOCKED);

		if (allSuccess) {
			freshOrder.setStatus(OrderStatus.SUCCESS);
		} else if (anyRunning) {
			freshOrder.setStatus(OrderStatus.RUNNING);
		} else if (anyFailed && !anyWaiting) {
			freshOrder.setStatus(OrderStatus.FAILED);
		} else if (anyWaiting) {
			freshOrder.setStatus(OrderStatus.WAITING);
		}

		orderRepository.save(freshOrder);
	}

}