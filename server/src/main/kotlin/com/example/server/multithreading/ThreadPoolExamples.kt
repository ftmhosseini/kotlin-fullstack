package com.example.server.multithreading

import java.util.concurrent.*

// ─────────────────────────────────────────────────────────────────────────────
// THREAD POOL EXAMPLES
//
// Creating raw threads is expensive. A thread pool creates N threads once
// and reuses them for many tasks — much more efficient.
// ─────────────────────────────────────────────────────────────────────────────

object ThreadPoolExamples {

    // ── Fixed Thread Pool ─────────────────────────────────────────────────────
    // 4 threads available. If 10 tasks submitted, 4 run immediately,
    // the other 6 wait in a queue.
    fun runWithFixedPool(): String {
        val pool = Executors.newFixedThreadPool(4)
        val results = mutableListOf<Future<String>>()

        repeat(8) { i ->
            val future = pool.submit(Callable {
                val threadName = Thread.currentThread().name
                Thread.sleep(100) // simulate work
                "Task $i done on $threadName"
            })
            results.add(future)
        }

        pool.shutdown()
        pool.awaitTermination(10, TimeUnit.SECONDS)

        return results.joinToString("\n") { it.get() }
    }

    // ── Cached Thread Pool ────────────────────────────────────────────────────
    // Creates new threads as needed, reuses idle ones.
    // Good for many short-lived tasks. Bad for long tasks (can create too many threads).
    fun runWithCachedPool(): String {
        val pool = Executors.newCachedThreadPool()
        val latch = CountDownLatch(3)
        val results = ConcurrentLinkedQueue<String>()

        repeat(3) { i ->
            pool.submit {
                results.add("Cached task $i on ${Thread.currentThread().name}")
                latch.countDown()
            }
        }

        latch.await() // wait for all 3 tasks to finish
        pool.shutdown()
        return results.joinToString("\n")
    }

    // ── CountDownLatch — wait for N tasks ─────────────────────────────────────
    // Classic use case: wait for multiple service calls to complete before continuing.
    // Like: wait for payment, inventory, and email services to all respond.
    fun demoCountDownLatch(): String {
        val latch = CountDownLatch(3)
        val pool = Executors.newFixedThreadPool(3)
        val results = ConcurrentLinkedQueue<String>()

        // Simulate 3 parallel service calls
        val services = listOf("PaymentService", "InventoryService", "EmailService")
        services.forEach { serviceName ->
            pool.submit {
                Thread.sleep((100..300L).random()) // simulate varying response times
                results.add("$serviceName responded")
                latch.countDown() // signal one task is done
            }
        }

        latch.await() // BLOCKS here until count reaches 0
        pool.shutdown()
        return "All services responded:\n" + results.joinToString("\n")
    }

    // ── Semaphore — limit concurrent access ───────────────────────────────────
    // Only N tasks can run at the same time (e.g., max 3 DB connections at once).
    fun demoSemaphore(): String {
        val semaphore = Semaphore(3) // only 3 threads at a time
        val pool = Executors.newFixedThreadPool(10)
        val results = ConcurrentLinkedQueue<String>()

        repeat(10) { i ->
            pool.submit {
                semaphore.acquire() // wait for a permit
                try {
                    results.add("Task $i using DB connection (${semaphore.availablePermits()} permits left)")
                    Thread.sleep(50) // simulate DB work
                } finally {
                    semaphore.release() // always release, even on exception
                }
            }
        }

        pool.shutdown()
        pool.awaitTermination(10, TimeUnit.SECONDS)
        return results.joinToString("\n")
    }
}
