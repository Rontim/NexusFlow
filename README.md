
<div align="center">

# ⚡ NexusFlow
### Multi-Tenant Distributed Order & Inventory Platform

[![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-Tested-yellow?style=for-the-badge&logo=docker)](https://www.testcontainers.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

*An enterprise-grade, multi-tenant e-commerce backend built from first principles to handle high-concurrency flash sales, strict data isolation, and non-blocking asynchronous workflows.*

</div>

---

## 🎯 Executive Summary

**NexusFlow** is engineered to mirror real-world B2B SaaS platforms (such as white-label multi-merchant e-commerce engines). It solves complex production backend challenges—including race-condition prevention during high-demand inventory drops, thread-safe multi-tenant isolation, and decoupled event-driven audit pipelines—without compromising latency or thread safety.

---

## 🏗️ Architectural Highlights

### 1. Row-Level Multi-Tenancy
* **Zero Data Leakage:** Every request is intercepted via a custom `TenantInterceptorFilter` that extracts tenant claims from stateless JWTs and binds them to a thread-safe `TenantContext`.
* **Scoped Queries:** Repositories and domain logic strictly enforce tenant isolation, ensuring Merchant A can never query or mutate Merchant B's inventory, products, or orders.

### 2. Concurrency-Safe Inventory Control (Pessimistic Locking)
* **Oversell Prevention:** During high-traffic flash sales or limited drops, standard database reads lead to race conditions. NexusFlow utilizes database-level **Pessimistic Write Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on inventory rows.
* **Guaranteed Integrity:** Concurrent threads competing for the final item queue up safely; inventory hits absolute zero (`0`) without a single oversell or phantom stock error.

### 3. Strict Order State Machine & Lifecycle
* Enforces explicit transitions across an `OrderStatus` enum (`RESERVED` $\rightarrow$ `PAID` $\rightarrow$ `FULFILLED` / `CANCELLED`).
* **Automated Rollbacks:** Cancelling an order automatically triggers inventory restoration, safely refunding reserved items back into the merchant's active stock pool within an atomic transaction.

### 4. Asynchronous Event-Driven Architecture
* **Non-Blocking Audit Trails:** Core checkout operations commit instantly. Auxiliary tasks—such as audit logging and notification triggers—are dispatched asynchronously using Spring's `@Async` and `@TransactionalEventListener` to keep user-facing APIs lightning-fast.

---

## 🛠️ Tech Stack & Tooling

* **Language & Framework:** Java 21, Spring Boot 4.1.2
* **Security & Auth:** Spring Security 6, Stateless JWT (JJWT 0.12.5), BCrypt
* **Persistence & Migrations:** Spring Data JPA, Hibernate, PostgreSQL, Flyway (version-controlled declarative SQL migrations)
* **Architecture Style:** Hexagonal/Clean Architecture with vertical domain slicing (`com.imanistack.tenant.*`)
* **Testing & Reliability:** JUnit 5, Spring Boot Test Slices, **Testcontainers** (spinning up ephemeral PostgreSQL containers for concurrent integration testing)
* **Boilerplate Reduction:** Lombok

---

## 🧪 Rigorous Engineering & Integration Testing

NexusFlow avoids fragile mock-based unit tests for critical business paths. Instead, it relies on **Testcontainers** to prove architectural correctness under live database pressure. 

For instance, our concurrency test suite spawns **10 concurrent threads** attempting to purchase items simultaneously against a real PostgreSQL container with limited stock, proving that locking mechanics behave flawlessly under heavy load:

```java
@Test
@DisplayName("High Concurrency Test: Only 3 out of 10 concurrent requests succeed when stock is 3")
void testConcurrentOrderPlacement() throws InterruptedException {
    // Multi-threaded ExecutorService firing simultaneous checkout requests...
    assertEquals(3, successfulOrders.get());
    assertEquals(7, failedOrders.get()); // Safely rejected with InsufficientStockException
    assertEquals(0, finalInventory.getQuantity()); // Zero oversells
}

```

---

## 🚀 Getting Started

### Prerequisites

* **Java 21 JDK** installed
* **Docker Desktop** running (required for local PostgreSQL and Testcontainers integration tests)

### Quick Run

1. Clone the repository:
```bash
git clone [https://github.com/Rontim/NexusFlow.git](https://github.com/Rontim/NexusFlow.git)
cd NexusFlow

```


2. Build and run tests to verify containerized integration suites:
```bash
./gradlew test

```


3. Run the application:
```bash
./gradlew bootRun

```



---

## 📄 Project Structure

```text
    src/main/java/com/imanistack/
    ├── shared/
    │   ├── infrastructure/
    │   │   ├── exception/          # Global Exception Handler & Custom Domain Exceptions
    │   │   └── tenant/             # Thread-safe TenantContext & Interceptor Filters
    ├── tenant/
    │   ├── auth/                   # JWT Security, Tenant Onboarding, UserType RBAC
    │   ├── product/                # Product Catalog & Pessimistic Locking Inventory Service
    │   └── order/                  # Order State Machine, DTOs, Event Listeners & Controllers

```

---

## 🛡️ License

Distributed under the MIT License. See `LICENSE` for more information.
