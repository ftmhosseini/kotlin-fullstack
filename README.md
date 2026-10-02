# Kotlin Full-Stack Project
### Android App + Spring Boot Server + Multithreading + Microservices + Docker

---

## Overview

This project is a full-stack Kotlin application covering real-world backend topics:

```
┌─────────────────────┐        HTTP / JSON        ┌──────────────────────────────────┐
│   Android App       │ ◄────────────────────────► │   Spring Boot Server             │
│                     │                             │                                  │
│  Jetpack Compose UI │                             │  REST API  /api/users            │
│  Retrofit (HTTP)    │                             │  REST API  /api/products         │
│  ViewModel          │                             │  Multithreading /api/multithreading│
│  Coroutines         │                             │  Microservices /api/microservices│
│                     │                             │  JPA + H2 / PostgreSQL           │
└─────────────────────┘                             └──────────────────────────────────┘
                                                              ▲
                                                              │
                                                    ┌─────────────────┐
                                                    │     Docker      │
                                                    │  server + DB    │
                                                    └─────────────────┘
```

---

## Project Structure

```
kotlin-fullstack/
│
├── Dockerfile                               ← builds server into a Docker image
├── docker-compose.yml                       ← runs server + PostgreSQL together
│
├── shared/                                  ← shared data models
│   └── src/main/kotlin/com/example/shared/
│       └── Models.kt                        ← User, Product, ApiResponse, Validator
│
├── server/
│   └── src/main/kotlin/com/example/server/
│       ├── ServerApplication.kt             ← entry point
│       ├── controller/
│       │   ├── UserController.kt            ← /api/users endpoints
│       │   └── ProductController.kt         ← /api/products endpoints
│       ├── service/
│       │   ├── UserService.kt               ← business logic
│       │   └── ProductService.kt
│       ├── repository/
│       │   ├── UserRepository.kt            ← database queries
│       │   └── ProductRepository.kt
│       ├── model/
│       │   ├── UserEntity.kt                ← JPA entity → DB table
│       │   └── ProductEntity.kt
│       │
│       ├── multithreading/                  ← ✨ NEW — multithreading examples
│       │   ├── CounterExamples.kt           ← race condition + 3 fixes
│       │   ├── ThreadPoolExamples.kt        ← ExecutorService, CountDownLatch, Semaphore
│       │   ├── CompletableFutureExamples.kt ← async chain, parallel, allOf, anyOf
│       │   └── MultithreadingController.kt  ← /api/multithreading endpoints
│       │
│       └── microservices/                   ← ✨ NEW — microservices patterns
│           ├── ServiceClients.kt            ← 3 simulated service clients
│           ├── OrderOrchestrator.kt         ← calls 3 services (sequential + parallel)
│           ├── CircuitBreakerService.kt     ← circuit breaker: CLOSED/OPEN/HALF_OPEN
│           └── MicroservicesController.kt   ← /api/microservices endpoints
│
└── android/
    └── app/src/main/kotlin/com/example/androidapp/
        ├── MainActivity.kt                  ← Jetpack Compose UI (Users + Products)
        ├── model/Models.kt                  ← local data classes
        ├── ui/
        │   ├── UserViewModel.kt
        │   └── ProductViewModel.kt
        └── network/
            ├── ApiService.kt                ← Retrofit interface
            └── RetrofitClient.kt
```

---

## Part 1 — Spring Boot Server (Users & Products)

### 3-Layer Architecture

```
HTTP Request
     ↓
┌─────────────┐
│ Controller  │  ← receives HTTP, returns HTTP (@GetMapping, @PostMapping...)
└──────┬──────┘
       ↓
┌─────────────┐
│   Service   │  ← business logic + validation
└──────┬──────┘
       ↓
┌─────────────┐
│ Repository  │  ← database queries (Spring generates SQL automatically)
└─────────────┘
       ↓
  Database (H2 in dev / PostgreSQL in production)
```

### API Endpoints

| Method | URL | What it does |
|--------|-----|-------------|
| GET    | /api/users | Get all users |
| GET    | /api/users/{id} | Get user by ID |
| POST   | /api/users | Create new user |
| PUT    | /api/users/{id} | Update user |
| DELETE | /api/users/{id} | Delete user |
| GET    | /api/users/search?name=ali | Search by name |
| GET    | /api/products | Get all products |
| POST   | /api/products | Create product |
| PUT    | /api/products/{id} | Update product |
| DELETE | /api/products/{id} | Delete product |
| GET    | /api/products/search?name=phone | Search products |
| GET    | /api/products/under-price?price=100 | Products under price |

### How to Run

```bash
cd /Users/fatemeh/Desktop/kotlin-fullstack
./gradlew :server:bootRun
```

Server: `http://localhost:8080` | H2 Console: `http://localhost:8080/h2-console`

```bash
# Quick test
curl http://localhost:8080/api/users
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Ali","email":"ali@example.com","age":25}'
```

---

## Part 2 — Multithreading

### What is Multithreading?
Running multiple tasks at the same time. A web server handles 1000 users simultaneously — each request runs on its own thread.

### The Race Condition Problem

`counter++` looks like one operation but is actually 3 steps: **READ → ADD 1 → WRITE**.
Two threads can both READ the same value before either WRITES → lost updates!

```
Thread 1: READ (count=5)
Thread 2: READ (count=5)   ← reads BEFORE Thread 1 writes!
Thread 1: WRITE (count=6)
Thread 2: WRITE (count=6)  ← should be 7, but both wrote 6!
```

### The 3 Fixes

| Fix | How | When to use |
|---|---|---|
| `synchronized` | Only 1 thread enters at a time | Simple shared state |
| `AtomicInteger` | CPU-level atomic operation, no locking | Counters — fastest |
| `ReentrantLock` | Manual lock with `tryLock` / timeout | Need more control |

### Try it — See the Race Condition Live

```bash
# See broken counter vs atomic vs synchronized — expected 1000, broken will be less!
curl http://localhost:8080/api/multithreading/counter-demo

# ExecutorService — 8 tasks on a pool of 4 threads
curl http://localhost:8080/api/multithreading/thread-pool

# CountDownLatch — wait for 3 parallel service calls
curl http://localhost:8080/api/multithreading/countdown-latch

# Semaphore — only 3 tasks can use the DB at a time
curl http://localhost:8080/api/multithreading/semaphore

# CompletableFuture — async chain + two tasks in parallel
curl http://localhost:8080/api/multithreading/completable

# allOf — run 5 tasks in parallel, wait for ALL
curl http://localhost:8080/api/multithreading/all-of

# anyOf — race 3 services, return whichever responds FIRST
curl http://localhost:8080/api/multithreading/any-of
```

### Key Concepts

**ExecutorService — always use a thread pool, never raw threads:**
```kotlin
// ❌ Expensive — creates a new OS thread every time
Thread { doWork() }.start()

// ✅ Reuses threads from pool — much more efficient
val pool = Executors.newFixedThreadPool(4)
pool.submit { doWork() }
```

**CompletableFuture — run tasks in background and chain results:**
```kotlin
CompletableFuture
    .supplyAsync { fetchUser(id) }       // runs in background
    .thenApply   { user -> enrich(user) } // chain when done
    .exceptionally { ex -> fallback() }   // handle errors
    .get()                                // wait for result
```

**CountDownLatch — wait for N things to complete:**
```kotlin
val latch = CountDownLatch(3)
pool.submit { callService1(); latch.countDown() }
pool.submit { callService2(); latch.countDown() }
pool.submit { callService3(); latch.countDown() }
latch.await() // blocks until all 3 call countDown()
processResults()
```

**Semaphore — limit concurrent access:**
```kotlin
val semaphore = Semaphore(3) // max 3 threads at a time
semaphore.acquire()
try { accessDatabase() }
finally { semaphore.release() }
```

---

## Part 3 — Microservices

### What are Microservices?
Instead of one big application (monolith), you split it into small independent services.
Each service has its own codebase, database, and runs on its own port/container.

```
Monolith:                        Microservices:
┌────────────────────┐           ┌──────────────┐  ┌──────────────┐
│  Users             │           │ user-service  │  │order-service │
│  Orders            │    →      │   port 8081  │  │  port 8080   │
│  Payments          │           └──────────────┘  └──────────────┘
│  Inventory         │           ┌──────────────┐  ┌──────────────┐
└────────────────────┘           │payment-service│  │inventory-svc │
                                 │   port 8083  │  │  port 8082   │
                                 └──────────────┘  └──────────────┘
```

### Simulated Services in This Project

| Service | Simulated port | Responsibility |
|---|---|---|
| user-service | 8081 | User accounts |
| inventory-service | 8082 | Stock management |
| payment-service | 8083 | Payment processing |
| order-service | 8080 | Orchestrates the full order flow |

### Patterns Implemented

**1. Service Orchestration — call multiple services to fulfill one request:**
```
Order Request
     ↓
1. Validate user   (user-service)         → sequential, must pass first
     ↓
2. Check stock  ╗                         → parallel! independent of each other
3. Get price    ╝  (parallel)
     ↓
4. Charge payment  (payment-service)      → sequential, needs price from step 3
     ↓
5. Reserve stock   (inventory-service)    → sequential, needs payment confirmation
```

**2. Circuit Breaker — fail fast when a service is down:**

```
CLOSED → OPEN → HALF_OPEN → CLOSED (recovered)

CLOSED:    Normal — calls go through ✅
OPEN:      Broken — return fallback IMMEDIATELY, don't even try ⛔
HALF_OPEN: Testing — allow 1 call to check if service recovered 🔄
```

Without circuit breaker: every call to a down service blocks for 30s timeout → cascading failures.
With circuit breaker: after 3 failures, return fallback in <1ms → system stays responsive.

### Try it

```bash
# Full order flow — calls user + inventory + payment services
curl "http://localhost:8080/api/microservices/order?userId=1&productId=1&quantity=2"

# Circuit breaker — normal call
curl "http://localhost:8080/api/microservices/circuit-breaker?fail=false"

# Circuit breaker — simulate failures (run 3 times to open the circuit)
curl "http://localhost:8080/api/microservices/circuit-breaker?fail=true"
curl "http://localhost:8080/api/microservices/circuit-breaker?fail=true"
curl "http://localhost:8080/api/microservices/circuit-breaker?fail=true"
# Circuit is now OPEN — next call returns fallback instantly

# Check circuit state
curl http://localhost:8080/api/microservices/circuit-breaker/status

# Health check (every microservice needs this)
curl http://localhost:8080/api/microservices/health

# Architecture overview
curl http://localhost:8080/api/microservices/service-info
```

### Microservices Communication

| Type | Technology | Use when |
|---|---|---|
| Synchronous | REST (HTTP) | You need an immediate response |
| Asynchronous | Kafka / RabbitMQ | Decouple services, handle traffic spikes |

---

## Part 4 — Android App

### What the app does
- **Users screen** — list, add, delete, search users (calls Spring Boot API)
- **Products screen** — list, add, delete, search products
- **Bottom navigation** — switch between both screens

### Key Technologies

| Technology | Purpose |
|---|---|
| Jetpack Compose | Modern UI — no XML, UI written in Kotlin |
| Retrofit | HTTP client — calls the Spring Boot REST API |
| Coroutines | Async — network calls without freezing the UI |
| ViewModel + StateFlow | State management — UI auto-updates when data changes |

### How to Run

1. Open Android Studio → `File → Open` → select the `android/` subfolder
2. **Sync Now** when prompted
3. Start the Spring Boot server first
4. Start an emulator → press **Run ▶**

The app connects to `http://10.0.2.2:8080` (emulator's route to your Mac's localhost).

### Common Errors & Fixes

| Error | Fix |
|---|---|
| `Port 8080 already in use` | `lsof -ti:8080 \| xargs kill -9` |
| `OutOfMemoryError` during build | Set `org.gradle.jvmargs=-Xmx4096m` in `gradle.properties` |
| `Theme not found` | Check `res/values/themes.xml` exists |
| `Plugin not found` | Open `android/` subfolder, NOT root folder |

---

## Part 5 — Docker

### Two Files

**`Dockerfile`** — 2-stage build (compile → run):
```
Stage 1: Gradle + JDK → compiles Kotlin → JAR file
Stage 2: Lightweight JRE → runs the JAR (no build tools = smaller image)
```

**`docker-compose.yml`** — server + PostgreSQL together:
```
┌──────────────────┐     ┌──────────────────────┐
│   postgres:5432  │ ←── │  server:8080          │
│   data persists  │     │  waits for DB ready   │
└──────────────────┘     └──────────────────────┘
```

### Run with Docker

```bash
cd /Users/fatemeh/Desktop/kotlin-fullstack
docker-compose up --build       # start everything
docker-compose down             # stop
docker-compose down -v          # stop + delete data
```

### Local vs Docker

| | `./gradlew :server:bootRun` | `docker-compose up` |
|---|---|---|
| Database | H2 in-memory (wiped on restart) | PostgreSQL (persists) |
| Use for | Development | Production-like testing |

---

## All Endpoints at a Glance

```bash
# ── Users ─────────────────────────────────────────────────────
GET    /api/users
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
GET    /api/users/search?name=

# ── Products ──────────────────────────────────────────────────
GET    /api/products
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
GET    /api/products/search?name=
GET    /api/products/under-price?price=

# ── Multithreading ────────────────────────────────────────────
GET    /api/multithreading/counter-demo      ← race condition demo
GET    /api/multithreading/thread-pool       ← ExecutorService
GET    /api/multithreading/countdown-latch   ← wait for N tasks
GET    /api/multithreading/semaphore         ← limit concurrency
GET    /api/multithreading/completable       ← async chain
GET    /api/multithreading/all-of            ← parallel all
GET    /api/multithreading/any-of            ← fastest wins

# ── Microservices ─────────────────────────────────────────────
GET    /api/microservices/order?userId=1&productId=1&quantity=2
GET    /api/microservices/circuit-breaker?fail=false
GET    /api/microservices/circuit-breaker/status
GET    /api/microservices/health
GET    /api/microservices/service-info
```

---

## Technologies Used

| Technology | Where | Purpose |
|---|---|---|
| Kotlin | Both | Main language |
| Spring Boot | Server | REST API framework |
| JPA + Hibernate | Server | Database ORM |
| H2 | Server (dev) | In-memory database |
| PostgreSQL | Docker | Production database |
| ExecutorService | Server | Thread pool management |
| AtomicInteger | Server | Thread-safe counters |
| CompletableFuture | Server | Async task chaining |
| CountDownLatch | Server | Wait for N tasks |
| Semaphore | Server | Limit concurrency |
| Circuit Breaker | Server | Fault tolerance pattern |
| Jetpack Compose | Android | Modern UI |
| Retrofit | Android | HTTP client |
| Coroutines | Android | Async operations |
| ViewModel + StateFlow | Android | Reactive state |
| Docker | DevOps | Containerized deployment |

---

## Next Steps

- [ ] Add JWT authentication (Spring Security)
- [ ] Replace simulated service clients with real HTTP calls (RestTemplate / WebClient)
- [ ] Add Kafka for async event-driven communication between services
- [ ] Switch to Kotlin Multiplatform to truly share models
- [ ] Deploy to Railway or AWS using the Docker image
- [ ] Add unit tests for Service and multithreading classes
- [ ] Add Products screen improvements (edit, filter by price)
