package com.example.server.microservices

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// ─────────────────────────────────────────────────────────────────────────────
// MicroservicesController
//
// REST endpoints to demonstrate microservices patterns in action.
//
// GET /api/microservices/order          → full order flow (calls 3 services)
// GET /api/microservices/circuit-breaker → circuit breaker pattern
// GET /api/microservices/health         → health check endpoint
// GET /api/microservices/service-info   → service discovery info
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/microservices")
class MicroservicesController(
    private val orderOrchestrator: OrderOrchestrator,
    private val circuitBreakerService: CircuitBreakerService
) {

    // ── Full Order Flow ───────────────────────────────────────────────────────
    // Simulates calling 3 separate microservices to fulfill an order:
    // UserService → InventoryService → PaymentService
    @GetMapping("/order")
    fun placeOrder(
        @RequestParam userId: Long = 1L,
        @RequestParam productId: Long = 1L,
        @RequestParam quantity: Int = 2
    ): ResponseEntity<Map<String, Any>> {
        val result = orderOrchestrator.placeOrder(userId, productId, quantity)
        return ResponseEntity.ok(result)
    }

    // ── Circuit Breaker ───────────────────────────────────────────────────────
    // Try calling a flaky service with circuit breaker protection.
    // After 3 failures, the circuit OPENS and returns fallback instantly.
    @GetMapping("/circuit-breaker")
    fun circuitBreaker(
        @RequestParam fail: Boolean = false
    ): ResponseEntity<Map<String, Any>> {
        val result = circuitBreakerService.callWithCircuitBreaker(shouldFail = fail)
        return ResponseEntity.ok(result)
    }

    // ── Circuit Breaker Status ────────────────────────────────────────────────
    @GetMapping("/circuit-breaker/status")
    fun circuitBreakerStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(circuitBreakerService.getStatus())
    }

    // ── Health Check ──────────────────────────────────────────────────────────
    // Every microservice exposes a /health endpoint.
    // Load balancers and Kubernetes use this to know if the service is alive.
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status"  to "UP",
            "service" to "order-service",
            "version" to "1.0.0",
            "checks"  to mapOf(
                "database"   to "UP",
                "cache"      to "UP",
                "downstream" to "UP"
            )
        ))
    }

    // ── Service Info ──────────────────────────────────────────────────────────
    // Explains the microservices architecture of this demo
    @GetMapping("/service-info")
    fun serviceInfo(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "architecture" to "Microservices",
            "services" to listOf(
                mapOf("name" to "user-service",      "port" to 8081, "responsibility" to "User accounts and authentication"),
                mapOf("name" to "inventory-service", "port" to 8082, "responsibility" to "Product stock management"),
                mapOf("name" to "payment-service",   "port" to 8083, "responsibility" to "Payment processing"),
                mapOf("name" to "order-service",     "port" to 8080, "responsibility" to "Orchestrates the full order flow"),
            ),
            "patterns_used" to listOf(
                "API Gateway — single entry point for all clients",
                "Circuit Breaker — fail fast when a service is down",
                "Service Discovery — services find each other by name",
                "Health Check — each service reports its status",
                "Event-Driven — services communicate via events/messages",
                "Saga Pattern — distributed transactions without 2PC"
            ),
            "communication" to mapOf(
                "sync"  to "REST (HTTP) — used when you need an immediate response",
                "async" to "Kafka/RabbitMQ — used for events and decoupling"
            )
        ))
    }
}
