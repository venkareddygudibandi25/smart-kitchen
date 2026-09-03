# Architectural Tradeoffs & Production Decisions

This document details key architectural decisions made for the Smart Kitchen application, highlighting the alternatives considered and the reasoning behind each choice.

---

## 1. Local Fixed ThreadPool vs. Virtual Threads + Distributed Queues

| Approach | Chosen Option: Configurable Fixed ThreadPool | Alternative Option: Java 21 Virtual Threads + Kafka |
|---|---|---|
| **Mechanism** | `Executors.newFixedThreadPool(N)` with size `scheduler.concurrency=N`. | `Executors.newVirtualThreadPerTaskExecutor()` + Redis Semaphore + Apache Kafka. |
| **Rationale** | Strictly enforces the assignment requirement ("Never run more than N tasks at the same time") in a single-node Spring Boot service without requiring external infrastructure. | Better for multi-node Swiggy-scale clusters, but introduces infrastructure complexity unnecessary for a single-node runner. |

---

## 2. DB Polling Scheduler vs. Event-Driven Messaging

| Approach | Chosen Option: Database Polling (`@Scheduled`) | Alternative Option: Event-Driven Push (Spring Events / ActiveMQ) |
|---|---|---|
| **Mechanism** | `@Scheduled(fixedDelay = 1000)` queries `WAITING` items from PostgreSQL every 1s. | In-memory event bus triggers execution immediately upon order creation or task completion. |
| **Rationale** | Highly resilient to server restarts. If the application crashes, polling seamlessly picks up remaining tasks on boot without losing event state. | Faster execution trigger (sub-millisecond), but requires persistent event queues to survive unexpected crashes. |

---

## 3. In-Memory DFS 3-Coloring vs. External Graph Database

| Approach | Chosen Option: In-Memory DFS Cycle Detection | Alternative Option: Graph Database (Neo4j / Amazon Neptune) |
|---|---|---|
| **Mechanism** | Standard DFS 3-coloring algorithm running in $O(V + E)$ time at HTTP POST submission. | Storing task dependency DAGs in a graph database and querying graph cycles via Cypher queries. |
| **Rationale** | Instant validation (~1ms) with zero latency overhead or external DB dependencies. Ideal for per-order task dependency chains. | Overkill for single-order item constraints; adds network hops and operational overhead. |

---

## 4. Immediate Chef Release on Cancel vs. Lazy Task Cleanup

| Approach | Chosen Option: Immediate Chef Release & Task Cascade | Alternative Option: Lazy Task Cancellation on Completion |
|---|---|---|
| **Mechanism** | `cancelOrder(id)` immediately marks items `CANCELLED` and sets assigned chefs to `available = true`. | Allowing mid-run cooking threads to finish before marking status `CANCELLED`. |
| **Rationale** | Immediately frees kitchen resources and worker thread slots for other waiting orders, avoiding wasted CPU and chef time. | Simpler thread interruption handling, but wastes valuable cooking capacity. |
