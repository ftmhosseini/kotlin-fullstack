package com.example.server.microservices

import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

// ─────────────────────────────────────────────────────────────────────────────
// CIRCUIT BREAKER PATTERN
//
// Problem: If ServiceB is down, ServiceA keeps calling it and each call
// blocks for a timeout (e.g., 30s). This wastes threads and slows everything.
//
// Solution: Circuit Breaker — after N failures, "open" the circuit and
// return a fallback INSTANTLY. Periodically try again to see if service recovered.
//
// 3 States:
//   CLOSED  → calls go through normally (circuit is "closed"/connected)
//   OPEN    → service is down, return fallback immediately (no actual call)
//   HALF_OPEN → testing if service recovered (allow 1 call through)
//
// ─────────────────────────────────────────────────────────────────────────────

enum class CircuitState { CLOSED, OPEN, HALF_OPEN }

@Service
class CircuitBreakerService {

    private var state = CircuitState.CLOSED
    private val failureCount = AtomicInteger(0)
    private val failureThreshold = 3         // open circuit after 3 failures
    private var lastFailureTime = 0L
    private val recoveryTimeMs = 5000L       // try recovery after 5 seconds

    fun callWithCircuitBreaker(shouldFail: Boolean): Map<String, Any> {
        // Check if we should try recovery (OPEN → HALF_OPEN)
        if (state == CircuitState.OPEN) {
            val timeSinceFailure = System.currentTimeMillis() - lastFailureTime
            if (timeSinceFailure >= recoveryTimeMs) {
                state = CircuitState.HALF_OPEN
            }
        }

        return when (state) {
            // ── OPEN: Don't even try — return fallback immediately ─────────────
            CircuitState.OPEN -> {
                val waitMs = recoveryTimeMs - (System.currentTimeMillis() - lastFailureTime)
                mapOf(
                    "circuit_state" to "OPEN",
                    "result"        to "FALLBACK: Service unavailable",
                    "failures"      to failureCount.get(),
                    "recovery_in_ms" to maxOf(0, waitMs),
                    "explanation"   to "Circuit is OPEN — not calling the service. Returns fallback instantly instead of waiting for timeout."
                )
            }

            // ── CLOSED or HALF_OPEN: Try the call ─────────────────────────────
            CircuitState.CLOSED, CircuitState.HALF_OPEN -> {
                try {
                    val result = callDownstreamService(shouldFail)
                    // Success — reset the circuit
                    onSuccess()
                    mapOf(
                        "circuit_state" to state.name,
                        "result"        to result,
                        "failures"      to failureCount.get(),
                        "explanation"   to "Call succeeded. Circuit remains/returns to CLOSED."
                    )
                } catch (e: Exception) {
                    // Failure — maybe open the circuit
                    onFailure()
                    mapOf(
                        "circuit_state"  to state.name,
                        "result"         to "FALLBACK: ${e.message}",
                        "failures"       to failureCount.get(),
                        "threshold"      to failureThreshold,
                        "explanation"    to if (state == CircuitState.OPEN)
                            "Circuit just OPENED after $failureThreshold failures!"
                        else
                            "Failure recorded. ${failureThreshold - failureCount.get()} more failures will open the circuit."
                    )
                }
            }
        }
    }

    // Simulated downstream service — throws if shouldFail=true
    private fun callDownstreamService(shouldFail: Boolean): String {
        Thread.sleep(50) // simulate network call
        if (shouldFail) throw RuntimeException("Downstream service is unavailable!")
        return "SUCCESS: Data from downstream service"
    }

    private fun onSuccess() {
        failureCount.set(0)
        state = CircuitState.CLOSED
    }

    private fun onFailure() {
        failureCount.incrementAndGet()
        lastFailureTime = System.currentTimeMillis()
        if (failureCount.get() >= failureThreshold) {
            state = CircuitState.OPEN
        }
    }

    fun getStatus(): Map<String, Any> = mapOf(
        "state"             to state.name,
        "failure_count"     to failureCount.get(),
        "failure_threshold" to failureThreshold,
        "recovery_time_ms"  to recoveryTimeMs,
        "explanation"       to mapOf(
            "CLOSED"    to "Normal — all calls go through",
            "OPEN"      to "Broken — returns fallback immediately, no calls made",
            "HALF_OPEN" to "Testing — one call allowed through to check recovery"
        )
    )
}
