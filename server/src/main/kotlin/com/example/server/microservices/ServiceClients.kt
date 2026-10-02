package com.example.server.microservices

import org.springframework.stereotype.Component

// ─────────────────────────────────────────────────────────────────────────────
// SIMULATED SERVICE CLIENTS
//
// In a real microservices setup, each of these would use RestTemplate or
// WebClient to make HTTP calls to separate services running on different ports.
//
// Here we simulate them with Thread.sleep() to mimic network latency.
//
// Real implementation would look like:
//   @Component
//   class UserServiceClient(private val restTemplate: RestTemplate) {
//       fun getUser(id: Long) = restTemplate.getForObject("http://user-service/users/$id", UserDto::class.java)
//   }
// ─────────────────────────────────────────────────────────────────────────────

// ── User Service Client ───────────────────────────────────────────────────────
@Component
class UserServiceClient {
    // Simulates: GET http://user-service:8081/users/{id}
    fun getUser(userId: Long): String {
        Thread.sleep(80) // simulate network call
        return "User{id=$userId, name=Alice, email=alice@example.com, verified=true}"
    }
}

// ── Inventory Service Client ──────────────────────────────────────────────────
@Component
class InventoryServiceClient {
    // Simulates: GET http://inventory-service:8082/products/{id}/stock
    fun checkStock(productId: Long, quantity: Int): String {
        Thread.sleep(120) // simulate network call
        val available = 50
        val hasStock = available >= quantity
        return "Product{id=$productId, available=$available, requested=$quantity, hasStock=$hasStock}"
    }

    // Simulates: POST http://inventory-service:8082/products/{id}/reserve
    fun reserve(productId: Long, quantity: Int): String {
        Thread.sleep(100)
        return "Reservation{productId=$productId, quantity=$quantity, reservationId=RES-${System.currentTimeMillis()}}"
    }
}

// ── Payment Service Client ────────────────────────────────────────────────────
@Component
class PaymentServiceClient {
    // Simulates: GET http://payment-service:8083/price?productId={id}&qty={qty}
    fun getPrice(productId: Long, quantity: Int): Double {
        Thread.sleep(100) // simulate network call
        val unitPrice = 29.99
        return unitPrice * quantity
    }

    // Simulates: POST http://payment-service:8083/charge
    fun charge(userId: Long, amount: Double): String {
        Thread.sleep(200) // payment processing takes longer
        return "Payment{userId=$userId, amount=$${"%.2f".format(amount)}, transactionId=TXN-${System.currentTimeMillis()}, status=SUCCESS}"
    }
}
