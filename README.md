# Smart Kitchen Order & Task Scheduler System

A high-performance Spring Boot 3.4 / Java 21 backend application that manages food order lifecycle, asynchronous chef assignment, task dependencies, exponential backoff retries, and HikariCP transaction optimization.

---

## 🏗️ System Architecture Diagram

```mermaid
graph TD
    Client[HTTP Client / Postman] -->|1. POST /orders| Controller[OrderController & AdminController]
    Controller -->|2. Validate & DFS Cycle Check| Service[OrderServiceImpl]
    Service -->|3. Save WAITING Order Graph| DB[(PostgreSQL Database)]

    subgraph Background Scheduler Execution
        Clock["@Scheduled(fixedDelay = 1000)<br>SmartKitchenScheduler"] -->|4. Poll WAITING Tasks| DB
        Clock -->|5. Reserve Chef & Mark RUNNING| DB
        Clock -->|6. Submit Async Task| Pool["ThreadPoolExecutor<br>(scheduler.concurrency=N)"]
        Pool -->|7. Thread.sleep(cookTime)<br>Async Cooking| Worker[Worker Thread]
        Worker -->|8. Complete Task & Release Chef| DB
    end

    subgraph System Recovery on Reboot
        Boot[Spring Boot Startup] -->|Reset Interrupted RUNNING Tasks| Recovery[StartupRecoveryRunner]
        Recovery -->|Mark WAITING & Release Chefs| DB
    end
```

---

## ⚙️ Concurrency & Scheduler Configuration

Concurrency, retries, and polling delays are fully configurable in `application.properties`:

```properties
# application.properties
server.port=8081

# Scheduler Concurrency & Retry Settings
scheduler.concurrency=3       # Maximum active cooking threads (N)
scheduler.max-retries=3       # Maximum retry attempts for failed items
scheduler.polling-delay=1000  # Poller check interval in milliseconds
```

In `SchedulerConfig.java`, the thread pool size is bound to `scheduler.concurrency`:

```java
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Value("${scheduler.concurrency:3}")
    private int concurrency;

    @Bean
    public ExecutorService executorService() {
        // Enforces strict limit of N concurrent cooking threads
        return Executors.newFixedThreadPool(concurrency);
    }
}
```

---

## 🌟 Key Features

1. **Task Dependency Chains & Cycle Detection**:
   - Supports parent-child task dependencies (e.g. *Bake Pizza Crust* before *Add Toppings*).
   - Rejects circular dependencies immediately at order placement using a **DFS 3-Coloring Algorithm** (HTTP `400 Bad Request`).
   - Automatically sets child tasks to `BLOCKED` if parent tasks fail or are cancelled.

2. **Transaction Polish & HikariCP Safety**:
   - Decouples database transactions from long `Thread.sleep()` cooking simulations.
   - Prevents database connection pool exhaustion under heavy concurrent load.

3. **Configurable Concurrency & FIFO Scheduling**:
   - Strict concurrency enforcement via `scheduler.concurrency=N` in `application.properties`.
   - Fair FIFO task assignment (`findByStatusOrderByIdAsc`).

4. **Restart Recovery**:
   - `StartupRecoveryRunner` detects interrupted `RUNNING` tasks on application boot, requeues them to `WAITING`, and releases assigned chefs.

5. **Real-time Stats API**:
   - `GET /stats` returns metrics on running/waiting tasks and available/busy chefs.

6. **Standardized API Response Wrapper**:
   - All REST APIs return `APIResponse<T>` (`statusCode`, `isError`, `result`).

---

## 🚀 Quick Start & How to Run

### Prerequisites
- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL** running on `localhost:5432` with database `smart_kitchen`

### Running the Application
```bash
./mvnw clean spring-boot:run
```
The application starts on port `8081`.

### Running Unit Tests
```bash
./mvnw test
```

---

## 📡 API Endpoint Overview

| Method | Endpoint | Description | Request Body / Query |
|---|---|---|---|
| `POST` | `/orders` | Place a new order (supports task dependencies) | `PlaceOrderRequest` JSON |
| `GET` | `/orders/{id}` | Get order status, items, & estimated completion time | Path variable `id` |
| `POST` | `/orders/{id}/cancel` | Cancel an order and release assigned chefs | Path variable `id` |
| `GET` | `/stats` | Real-time system metrics (running/waiting tasks, chef availability) | None |
| `POST` | `/admin/chefs` | Add a new chef | `CreateChefRequest` JSON |
| `GET` | `/admin/chefs` | List all chefs | None |
| `POST` | `/admin/menu` | Add a new menu item | `CreateMenuItemRequest` JSON |
| `GET` | `/admin/menu` | List all menu items | None |

---

## 📸 API Response Examples

### 1. Place an Order with Task Dependencies (`POST /orders`)

**Request Payload**:
```json
{
  "customerName": "Alice Smith",
  "items": [
    { "menuItemId": 1, "dependsOnIndex": null },
    { "menuItemId": 2, "dependsOnIndex": 0 }
  ]
}
```

**Response Payload (`201 Created`)**:
```json
{
  "statusCode": 201,
  "isError": false,
  "result": {
    "orderId": 1,
    "customerName": "Alice Smith",
    "status": "WAITING",
    "estimatedCompletionSeconds": 8,
    "items": [
      {
        "itemId": 1,
        "itemName": "Pizza Crust",
        "chefName": null,
        "status": "WAITING",
        "attempts": 0,
        "dependsOnItemId": null
      },
      {
        "itemId": 2,
        "itemName": "Pizza Toppings",
        "chefName": null,
        "status": "WAITING",
        "attempts": 0,
        "dependsOnItemId": 1
      }
    ]
  }
}
```

---

### 2. Poll Order Status (`GET /orders/1`)

**Response Payload (`200 OK`)**:
```json
{
  "statusCode": 200,
  "isError": false,
  "result": {
    "orderId": 1,
    "customerName": "Alice Smith",
    "status": "RUNNING",
    "estimatedCompletionSeconds": 3,
    "items": [
      {
        "itemId": 1,
        "itemName": "Pizza Crust",
        "chefName": "Chef Maya",
        "status": "SUCCESS",
        "attempts": 0,
        "dependsOnItemId": null
      },
      {
        "itemId": 2,
        "itemName": "Pizza Toppings",
        "chefName": "Chef Ram",
        "status": "RUNNING",
        "attempts": 0,
        "dependsOnItemId": 1
      }
    ]
  }
}
```

---

### 3. Get Real-time System Statistics (`GET /stats`)

**Response Payload (`200 OK`)**:
```json
{
  "statusCode": 200,
  "isError": false,
  "result": {
    "runningTasks": 1,
    "waitingTasks": 3,
    "availableChefs": 4,
    "busyChefs": 1
  }
}
```

---

### 4. Circular Dependency Error (`POST /orders` - Cycle Rejected)

**Request Payload with Cycle** (Item 0 depends on Item 1, Item 1 depends on Item 0):
```json
{
  "customerName": "Bob Johnson",
  "items": [
    { "menuItemId": 1, "dependsOnIndex": 1 },
    { "menuItemId": 2, "dependsOnIndex": 0 }
  ]
}
```

**Response Payload (`400 Bad Request`)**:
```json
{
  "statusCode": 400,
  "isError": true,
  "result": "Circular dependency detected in order tasks"
}
```

---

## 📚 Technical Documentation
- **[DESIGN.md](DESIGN.md)**: Answers to core interview architectural questions (concurrency limit, cycle detection, HikariCP transaction decoupling, restart recovery).
- **[TRADEOFFS.md](TRADEOFFS.md)**: Architectural trade-offs comparing local thread pool choices vs. Swiggy/DoorDash production scale.
