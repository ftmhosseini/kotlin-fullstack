package com.example.server.microservices

import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

// ─────────────────────────────────────────────────────────────────────────────
// OrderOrchestrator — simulates an order-service calling 3 other microservices
//
// In a real system each service would be a separate Spring Boot app on
// its own port/container. Here we simulate them with delays.
//
// Two approaches shown:
//   1. Sequential  — call one after another (slow)
//   2. Parallel    — call independent ones at the same time (fast)
// ─────────────────────────────────────────────────────────────────────────────
@Service
class OrderOrchestrator(
    private val userServiceClient: UserServiceClient,
    private val inventoryServiceClient: InventoryServiceClient,
    private val paymentServiceClient: PaymentServiceClient
) {
    private val pool = Executors.newFixedThreadPool(4)

    fun placeOrder(userId: Long, productId: Long, quantity: Int): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        val steps = mutableListOf<String>()

        // ── Step 1: Validate user (must succeed before continuing) ────────────
        steps.add("Step 1: Calling user-service to validate user $userId...")
        val user = userServiceClient.getUser(userId)
        steps.add("✅ user-service responded: $user")

        // ── Step 2 & 3: Check inventory AND get price in PARALLEL ─────────────
        // These two are independent — no need to do them sequentially!
        steps.add("Step 2+3: Calling inventory-service and payment-service in PARALLEL...")
        val inventoryFuture = CompletableFuture.supplyAsync(
            { inventoryServiceClient.checkStock(productId, quantity) }, pool
        )
        val priceFuture = CompletableFuture.supplyAsync(
            { paymentServiceClient.getPrice(productId, quantity) }, pool
        )

        // Wait for BOTH to finish
        val stock = inventoryFuture.get()
        val price = priceFuture.get()
        steps.add("✅ inventory-service: $stock")
        steps.add("✅ payment-price: $price")

        // ── Step 4: Process payment ───────────────────────────────────────────
        steps.add("Step 4: Calling payment-service to charge $price...")
        val payment = paymentServiceClient.charge(userId, price)
        steps.add("✅ payment-service: $payment")

        // ── Step 5: Reserve inventory ─────────────────────────────────────────
        steps.add("Step 5: Calling inventory-service to reserve $quantity units...")
        val reservation = inventoryServiceClient.reserve(productId, quantity)
        steps.add("✅ inventory-service reservation: $reservation")

        val elapsed = System.currentTimeMillis() - startTime

        return mapOf(
            "orderId"      to "ORD-${System.currentTimeMillis()}",
            "userId"       to userId,
            "productId"    to productId,
            "quantity"     to quantity,
            "totalPrice"   to price,
            "status"       to "CONFIRMED",
            "elapsed_ms"   to elapsed,
            "steps"        to steps,
            "note"         to "Steps 2+3 ran in parallel — faster than sequential!"
        )
    }
}
