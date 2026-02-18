# Ledger Core
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://openjdk.org/projects/jdk/25/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A high-performance, double-entry financial ledger engine designed for absolute consistency, auditability, and extreme reliability. This project serves as the "Source of Truth" for loan lifecycles, ensuring that every cent is accounted for across originations, repayments, and adjustments.



## The Mission
In fintech, "eventually consistent" is a failure state. **Ledger Core** is built to solve the **Lost Update** and **Inconsistent State** problems inherent in high-volume financial systems. It provides an immutable audit trail and guaranteed atomic state transitions.

## Architectural Excellence
This project utilizes **Hexagonal Architecture** (Ports and Adapters) to decouple business rules from infrastructure, ensuring the core logic remains testable and maintainable.

* **Domain Layer:** Uses the **State Machine** pattern to govern loan lifecycles (`PENDING` → `ACTIVE` → `CLOSED`).
* **Application Layer:** Orchestrates business use cases (Repayments, Originations) via transactional services.
* **Infrastructure Layer:** * **jOOQ:** Type-safe SQL execution for performance-critical financial queries.
    * **Flyway:** Version-controlled database migrations.
    * **PostgreSQL:** The ACID-compliant backbone.

## Critical Technical Features

### 1. Atomic Double-Entry Bookkeeping
Every transaction is atomic. We synchronize the **Loan Snapshot** update and the **Ledger Entry** creation within a single database transaction. This ensures the balance and the audit trail never drift.

### 2. Optimistic Concurrency Control
To handle high-frequency repayments, we implement version-based locking. This prevents "Lost Updates" by ensuring that if two threads attempt to update the same loan, only one succeeds, while the other is forced to re-validate.

### 3. Idempotency Guarantees
All mutation endpoints require an `X-Idempotency-Key`. This prevents duplicate charges/credits in the event of network retries—a non-negotiable requirement for financial software.


## Tech Stack
* **Backend:** Java 25, Spring Boot 4.0.2
* **Database:** PostgreSQL 16
* **Persistence:** jOOQ (Java Object Oriented Querying)
* **Migrations:** Flyway
* **Testing:** Testcontainers (Postgres), WebTestClient, Mockito, AssertJ
* **Build Tool:** Maven

## Testing Strategy
I follow a rigorous testing pyramid to ensure financial integrity:
* **Unit Tests:** Deep validation of the Loan State Machine and Money value objects.
* **Slice Tests:** `@WebMvcTest` for API contract validation and JSON serialization.
* **Integration Tests:** Utilizing **Testcontainers** to run tests against a real, ephemeral PostgreSQL instance.
* **Concurrency Torture Tests:** Stress-testing the ledger with parallel threads using `CompletableFuture` to prove thread-safety and locking mechanisms.

## Technical Decisions: Why jOOQ over JPA?
While Hibernate/JPA is standard, I chose **jOOQ** for this ledger for three reasons:
1.  **Transparency:** Financial systems require total control over SQL. jOOQ allows for explicit, optimized queries without the "magic" or overhead of a full ORM.
2.  **Type Safety:** jOOQ generates code based on the actual DB schema, catching mapping errors at compile-time rather than runtime.
3.  **Complex Reporting:** Ledger systems often require complex window functions and joins that are cumbersome in JPQL but native and fluid in jOOQ.

## Getting Started
### Prerequisites
* **Docker Desktop** (for Testcontainers)
* **JDK 25**

### Installation & Run
1. **Clone the repo:**
   ```bash
   git clone [https://github.com/d3rrick/ledger-core.git](https://github.com/d3rrick/ledger-core.git)
    
   # Generate migrations
   mvn clean generate-sources -Prun-migrations
   
   # Running test suite
   mvn verify
    ```

~ Derrick