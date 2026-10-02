package com.example.server.multithreading

import java.util.concurrent.atomic.AtomicInteger

// ─────────────────────────────────────────────────────────────────────────────
// MULTITHREADING EXAMPLES
//
// This file demonstrates the classic race condition problem and 3 ways to fix it.
// Run the demo via: GET /api/multithreading/counter-demo
// ─────────────────────────────────────────────────────────────────────────────

// ── 1. BROKEN — Race Condition ────────────────────────────────────────────────
// Two threads increment the same counter simultaneously.
// counter++ is NOT atomic — it is actually 3 steps: READ → ADD 1 → WRITE
// Both threads can READ the same value before either WRITES → lost updates!
object BrokenCounter {
    var count = 0  // NOT thread-safe

    fun increment() {
        count++    // READ, ADD, WRITE — 3 separate steps, not atomic!
    }
}

// ── 2. FIX 1 — synchronized ──────────────────────────────────────────────────
// Only ONE thread can execute the block at a time.
// Simple but can be slower under high contention.
object SynchronizedCounter {
    private var count = 0
    private val lock = Any()

    fun increment() {
        synchronized(lock) {   // only one thread enters at a time
            count++
        }
    }

    fun getCount() = synchronized(lock) { count }
}

// ── 3. FIX 2 — AtomicInteger ─────────────────────────────────────────────────
// Uses CPU-level atomic operations (Compare-And-Swap).
// No locking needed — faster than synchronized for simple counters.
object AtomicCounter {
    private val count = AtomicInteger(0)

    fun increment() {
        count.incrementAndGet()   // atomic — one indivisible operation
    }

    fun getCount() = count.get()
}

// ── 4. FIX 3 — @Synchronized Kotlin annotation ───────────────────────────────
// Kotlin's cleaner syntax for synchronized methods.
object KotlinSyncCounter {
    private var count = 0

    @Synchronized
    fun increment() {
        count++
    }

    @Synchronized
    fun getCount() = count
}
