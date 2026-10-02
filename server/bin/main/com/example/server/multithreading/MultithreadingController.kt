package com.example.server.multithreading

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

// ─────────────────────────────────────────────────────────────────────────────
// MultithreadingController
//
// REST endpoints to SEE multithreading in action.
// Run the server and call these endpoints to observe the results.
//
// GET /api/multithreading/counter-demo    → race condition vs atomic vs synchronized
// GET /api/multithreading/thread-pool     → ExecutorService with fixed pool
// GET /api/multithreading/countdown-latch → wait for multiple services
// GET /api/multithreading/semaphore       → limit concurrent DB access
// GET /api/multithreading/completable     → async chain + parallel tasks
// GET /api/multithreading/all-of          → run N tasks in parallel
// GET /api/multithreading/any-of          → race tasks, get first result
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/multithreading")
class MultithreadingController {

    // ── Counter Demo ──────────────────────────────────────────────────────────
    // Runs 1000 increments with BOTH a broken counter and an atomic counter.
    // Expected: 1000. Broken counter will likely be LESS due to race conditions.
    @GetMapping("/counter-demo")
    fun counterDemo(): ResponseEntity<Map<String, Any>> {
        val iterations = 1000
        val pool = Executors.newFixedThreadPool(10)

        // Reset counters
        BrokenCounter.count = 0
        val atomicCount = AtomicInteger(0)

        val latches = listOf(
            CountDownLatch(iterations),
            CountDownLatch(iterations),
            CountDownLatch(iterations)
        )

        repeat(iterations) {
            // Broken — race condition
            pool.submit { BrokenCounter.increment(); latches[0].countDown() }
            // Atomic — thread safe
            pool.submit { atomicCount.incrementAndGet(); latches[1].countDown() }
            // Synchronized — thread safe
            pool.submit { SynchronizedCounter.increment(); latches[2].countDown() }
        }

        latches.forEach { it.await() }
        pool.shutdown()

        return ResponseEntity.ok(mapOf(
            "expected"             to iterations,
            "broken_counter"       to BrokenCounter.count,
            "broken_is_wrong"      to (BrokenCounter.count != iterations),
            "atomic_counter"       to atomicCount.get(),
            "atomic_is_correct"    to (atomicCount.get() == iterations),
            "sync_counter"         to SynchronizedCounter.getCount(),
            "sync_is_correct"      to (SynchronizedCounter.getCount() == iterations),
            "explanation"          to "Broken counter loses updates because counter++ is READ+ADD+WRITE (3 steps). AtomicInteger and synchronized are always correct."
        ))
    }

    // ── Thread Pool ───────────────────────────────────────────────────────────
    @GetMapping("/thread-pool")
    fun threadPool(): ResponseEntity<Map<String, Any>> {
        val start = System.currentTimeMillis()
        val result = ThreadPoolExamples.runWithFixedPool()
        val elapsed = System.currentTimeMillis() - start

        return ResponseEntity.ok(mapOf(
            "description" to "8 tasks on a pool of 4 threads",
            "elapsed_ms"  to elapsed,
            "results"     to result.split("\n")
        ))
    }

    // ── CountDownLatch ────────────────────────────────────────────────────────
    @GetMapping("/countdown-latch")
    fun countdownLatch(): ResponseEntity<Map<String, Any>> {
        val start = System.currentTimeMillis()
        val result = ThreadPoolExamples.demoCountDownLatch()
        val elapsed = System.currentTimeMillis() - start

        return ResponseEntity.ok(mapOf(
            "description" to "Wait for 3 parallel service calls before continuing",
            "elapsed_ms"  to elapsed,
            "result"      to result.split("\n")
        ))
    }

    // ── Semaphore ─────────────────────────────────────────────────────────────
    @GetMapping("/semaphore")
    fun semaphore(): ResponseEntity<Map<String, Any>> {
        val result = ThreadPoolExamples.demoSemaphore()
        return ResponseEntity.ok(mapOf(
            "description" to "10 tasks but only 3 can access the DB at a time",
            "results"     to result.split("\n")
        ))
    }

    // ── CompletableFuture chain ───────────────────────────────────────────────
    @GetMapping("/completable")
    fun completable(): ResponseEntity<Map<String, Any>> {
        val start = System.currentTimeMillis()
        val chain    = CompletableFutureExamples.basicChain()
        val parallel = CompletableFutureExamples.parallelTasks()
        val elapsed  = System.currentTimeMillis() - start

        return ResponseEntity.ok(mapOf(
            "description"    to "CompletableFuture: chain steps + parallel tasks",
            "elapsed_ms"     to elapsed,
            "chain_result"   to chain,
            "parallel_result" to parallel
        ))
    }

    // ── allOf — wait for ALL ──────────────────────────────────────────────────
    @GetMapping("/all-of")
    fun allOf(): ResponseEntity<Map<String, Any>> {
        val start   = System.currentTimeMillis()
        val result  = CompletableFutureExamples.allOf()
        val elapsed = System.currentTimeMillis() - start

        return ResponseEntity.ok(mapOf(
            "description" to "Run 5 tasks in parallel, wait for ALL to finish",
            "elapsed_ms"  to elapsed,
            "results"     to result.split("\n")
        ))
    }

    // ── anyOf — get first result ──────────────────────────────────────────────
    @GetMapping("/any-of")
    fun anyOf(): ResponseEntity<Map<String, Any>> {
        val start   = System.currentTimeMillis()
        val result  = CompletableFutureExamples.anyOf()
        val elapsed = System.currentTimeMillis() - start

        return ResponseEntity.ok(mapOf(
            "description" to "Race 3 services — return whichever responds first",
            "elapsed_ms"  to elapsed,
            "winner"      to result
        ))
    }
}
