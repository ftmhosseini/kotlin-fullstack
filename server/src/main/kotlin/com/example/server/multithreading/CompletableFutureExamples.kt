package com.example.server.multithreading

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

// ─────────────────────────────────────────────────────────────────────────────
// COMPLETABLEFUTURE EXAMPLES
//
// CompletableFuture lets you run tasks in the background and chain operations
// without blocking the main thread.
//
// Think of it like: "start this task, when it's done do this, when that's done do that"
// ─────────────────────────────────────────────────────────────────────────────

object CompletableFutureExamples {

    private val pool = Executors.newFixedThreadPool(4)

    // ── Basic async task ──────────────────────────────────────────────────────
    // supplyAsync → runs in background
    // thenApply   → transforms the result (still background)
    // thenApply   → transforms again
    // get()       → blocks until done and returns final result
    fun basicChain(): String {
        return CompletableFuture
            .supplyAsync({ fetchUserFromDb(1L) }, pool)       // step 1: fetch user
            .thenApply  { user -> enrichWithProfile(user) }   // step 2: add profile data
            .thenApply  { enriched -> "Result: $enriched" }   // step 3: format
            .exceptionally { ex -> "Error: ${ex.message}" }   // handle any error
            .get()                                             // wait and get result
    }

    // ── Run two tasks in parallel, combine results ────────────────────────────
    // thenCombine runs BOTH futures in parallel and combines when BOTH are done.
    // Much faster than running sequentially.
    fun parallelTasks(): String {
        val userFuture = CompletableFuture.supplyAsync(
            { fetchUserFromDb(1L) }, pool
        )
        val orderFuture = CompletableFuture.supplyAsync(
            { fetchOrdersForUser(1L) }, pool
        )

        // waits for BOTH to complete, then combines
        return userFuture.thenCombine(orderFuture) { user, orders ->
            "User: $user | Orders: $orders"
        }.get()
    }

    // ── Run N tasks in parallel, wait for ALL ────────────────────────────────
    fun allOf(): String {
        val futures = (1..5).map { id ->
            CompletableFuture.supplyAsync({ fetchUserFromDb(id.toLong()) }, pool)
        }

        // allOf waits until ALL futures complete
        CompletableFuture.allOf(*futures.toTypedArray()).get()

        return futures.joinToString("\n") { it.get() }
    }

    // ── Run N tasks, get whichever finishes FIRST ─────────────────────────────
    fun anyOf(): String {
        val futures = listOf(
            CompletableFuture.supplyAsync({ slowService("ServiceA", 300L) }, pool),
            CompletableFuture.supplyAsync({ slowService("ServiceB", 100L) }, pool),
            CompletableFuture.supplyAsync({ slowService("ServiceC", 200L) }, pool),
        )

        // anyOf returns as soon as the FIRST one finishes
        return CompletableFuture.anyOf(*futures.toTypedArray()).get() as String
    }

    // ── Simulated service calls ───────────────────────────────────────────────
    private fun fetchUserFromDb(id: Long): String {
        Thread.sleep(100) // simulate DB query
        return "User#$id"
    }

    private fun enrichWithProfile(user: String): String {
        Thread.sleep(50)
        return "$user+Profile"
    }

    private fun fetchOrdersForUser(userId: Long): String {
        Thread.sleep(150)
        return "Orders[${userId}a,${userId}b]"
    }

    private fun slowService(name: String, delayMs: Long): String {
        Thread.sleep(delayMs)
        return "$name finished in ${delayMs}ms"
    }
}
