# Smart Kitchen Order & Task Scheduler System

A high-performance Spring Boot 3.4 / Java 21 backend application that manages food order lifecycle, asynchronous chef assignment, task dependencies, exponential backoff retries, and HikariCP transaction optimization.

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

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/orders` | Place a new order (supports item dependencies) |
| `GET` | `/orders/{id}` | Get order status, items, and estimated completion time |
| `POST` | `/orders/{id}/cancel` | Cancel an order and release chefs |
| `GET` | `/stats` | System statistics (running/waiting tasks, chef availability) |
| `POST` | `/admin/chefs` | Add a new chef |
| `GET` | `/admin/chefs` | List all chefs |
| `POST` | `/admin/menu` | Add a new menu item |
| `GET` | `/admin/menu` | List all menu items |

---

## 💡 Example API Requests

### 1. Place an Order with Task Dependencies (`POST /orders`)
```json
{
  "customerName": "Alice Smith",
  "items": [
    { "menuItemId": 1, "dependsOnIndex": null },
    { "menuItemId": 2, "dependsOnIndex": 0 }
  ]
}
```

### 2. Response Payload Format (`APIResponse<OrderResponse>`)
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

### 3. Get System Statistics (`GET /stats`)
```json
{
  "statusCode": 200,
  "isError": false,
  "result": {
    "runningTasks": 1,
    "waitingTasks": 3,
    "availableChefs": 2,
    "busyChefs": 1
  }
}
```

---

## 📚 Technical Documentation
- **[DESIGN.md](DESIGN.md)**: Deep-dive answers to core interview architectural questions (concurrency, cycle detection, HikariCP protection, restart recovery).
- **[TRADEOFFS.md](TRADEOFFS.md)**: Comparison of local implementation choices vs. Swiggy/DoorDash enterprise scale.
