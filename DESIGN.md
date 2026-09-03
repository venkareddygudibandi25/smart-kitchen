# Smart Kitchen System Design & Architecture

This document outlines the real-world scenario chosen, the custom improvement added, architectural decisions, and explicit answers to the four core design questions required by the assignment.

---

## 1. Scenario Chosen: Smart Kitchen Food Preparation Pipeline

Instead of generic terms like `task1` and `task2`, this application models a **Smart Commercial Kitchen Preparation System** (e.g., Cloud Kitchen / Swiggy / DoorDash execution engine):
- **Tasks** map to dish preparation items (e.g., *Bake Pizza Crust*, *Add Pizza Toppings*, *Grill Burger Patty*).
- **Chefs** represent available cooking resources (`available = true/false`).
- **Dependencies** represent real-world cooking sequences (e.g. *Bake Pizza Crust* must reach `SUCCESS` before *Add Pizza Toppings* can start).

---

## 2. One Custom Improvement Added

### Feature: Dynamic Estimated Completion Time (`estimatedCompletionSeconds`)
- **Problem Solved**: When customers check their order status (`GET /orders/{id}`), static statuses (`WAITING`, `RUNNING`) do not tell them when their food will be ready.
- **Solution**: `OrderResponse` dynamically calculates `estimatedCompletionSeconds` by summing the remaining cook times of all non-completed items in the order (`WAITING` or `RUNNING`). As dishes finish cooking, the remaining time counts down to 0 in real-time.
- **Bonus Improvement**: Implemented standardized `APIResponse<T>` wrapper (`statusCode`, `isError`, `result`) and global exception handling (`@RestControllerAdvice`) for enterprise-grade error reporting.

---

## 3. Answers to the Four Core Design Questions

### Question 1: Concurrency Enforcement & Race Condition Safety
> *How do you make sure your concurrency limit is never exceeded, even when many tasks are submitted at the same moment? Describe what could go wrong if you got this wrong.*

- **Enforcement**: Concurrency is strictly bounded by `scheduler.concurrency=N` in `application.properties`. `SchedulerConfig.java` configures a fixed `ExecutorService` thread pool (`Executors.newFixedThreadPool(N)`). Additionally, `SmartKitchenScheduler.assignTasks()` uses a `synchronized` block with short JPA transactions (`@Transactional`) to atomically match available chefs to waiting items.
- **What could go wrong**:
  1. **Thread/Connection Exhaustion**: If `Thread.sleep()` was executed inside a database transaction, HikariCP DB pool connections would be starved, causing HTTP `POST /orders` requests to fail with connection timeouts.
  2. **Double Assignment**: Without atomic reservation, two scheduler ticks running simultaneously could assign the same free chef to two different items.

---

### Question 2: Service Interruption & Restart Behavior
> *If the service is killed while tasks are running, what exactly happens when it starts again? Say clearly whether any work could be lost or accidentally run twice.*

- **Behavior on Restart**: `StartupRecoveryRunner` (`CommandLineRunner`) executes automatically when Spring Boot boots up. It queries PostgreSQL for items left in `RUNNING` status, resets their status to `WAITING`, clears assigned chefs, and marks all chefs as `available = true`.
- **Is work lost?**: **No**. Order definitions, completed items (`SUCCESS`), and item configurations are safely persisted in PostgreSQL.
- **Is work run twice?**: Mid-run items that were 50% cooked when the server stopped will restart cooking from 0 seconds (at-least-once execution semantics). Since task execution in our kitchen model is idempotent, this guarantees safe recovery without leaving tasks permanently stuck in `RUNNING` state.

---

### Question 3: Scheduling Rule & Edge-Case Failure
> *When several tasks are ready and one slot frees up, which one runs next? Explain your rule and give an example where it gives a poor result.*

- **Scheduling Rule**: **FIFO (First-In, First-Out)** ordered by task ID (`orderItemRepository.findByStatusOrderByIdAsc(WAITING)`).
- **Example where it gives a poor result (Head-of-Line Blocking / Starvation)**:
  - Suppose Order 1 submits three 60-second items (*Slow Roast Pork*), occupying all 3 thread slots.
  - Suppose Order 2 submits a 2-second item (*Espresso*) followed by a dependent item (*Serve Coffee*).
  - Under FIFO, the 2-second *Espresso* must wait 60 seconds behind the long items, creating poor customer latency. A Priority Queue or Shortest-Job-First (SJF) scheduler would yield better overall throughput in this edge case.

---

### Question 4: Fundamental Invariant & Enforcement Location
> *What is the one thing that must always be true for your service to be considered correct? Point to where in your code that is enforced.*

- **Fundamental Invariant**: **"A task must NEVER start running unless all of its parent dependencies have reached SUCCESS status, and active tasks must NEVER exceed N."**
- **Enforcement Locations**:
  1. **Dependency Execution Guard**: `SmartKitchenScheduler.isReadyToRun()` ([SmartKitchenScheduler.java:L94](file:///d:/smart-kitchen/src/main/java/com/smartkitchen/scheduler/SmartKitchenScheduler.java#L94)) returns `true` ONLY IF `dependsOnItem.getStatus() == OrderItemStatus.SUCCESS`.
  2. **Blocked Cascade Propagation**: `SmartKitchenScheduler.processBlockedTasks()` ([SmartKitchenScheduler.java:L71](file:///d:/smart-kitchen/src/main/java/com/smartkitchen/scheduler/SmartKitchenScheduler.java#L71)) transitions waiting items to `BLOCKED` if parent tasks fail or cancel.
  3. **Circular Dependency Rejection**: `OrderServiceImpl.validateCircularDependencies()` ([OrderServiceImpl.java:L114](file:///d:/smart-kitchen/src/main/java/com/smartkitchen/service/order/impl/OrderServiceImpl.java#L114)) uses DFS 3-coloring to reject cyclic graphs at HTTP POST submission time.
