package com.example.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// ─────────────────────────────────────────────────────────────────────────────
// ENTRY POINT
// @SpringBootApplication sets up everything automatically:
//   - component scanning (finds all @RestController, @Service, @Repository)
//   - auto-configuration (sets up database, web server, etc.)
// ─────────────────────────────────────────────────────────────────────────────
@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
    // Server starts on http://localhost:8080
}
