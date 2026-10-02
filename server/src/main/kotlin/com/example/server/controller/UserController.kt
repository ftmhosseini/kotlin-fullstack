package com.example.server.controller

import com.example.server.service.UserService
import com.example.shared.ApiResponse
import com.example.shared.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// ─────────────────────────────────────────────────────────────────────────────
// CONTROLLER — defines all HTTP endpoints for Users
//
// @RestController  → this class handles HTTP requests and returns JSON
// @RequestMapping  → all routes in this class start with /api/users
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService  // Spring injects this automatically
) {

    // GET /api/users
    // → returns all users
    @GetMapping
    fun getAllUsers(): ResponseEntity<ApiResponse<List<User>>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }

    // GET /api/users/1
    // → returns user with id=1
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<User>> {
        val response = userService.getUserById(id)
        return if (response.success) ResponseEntity.ok(response)
               else ResponseEntity.notFound().build()
    }

    // POST /api/users
    // Body: { "name": "Ali", "email": "ali@example.com", "age": 25 }
    // → creates a new user
    @PostMapping
    fun createUser(@RequestBody user: User): ResponseEntity<ApiResponse<User>> {
        val response = userService.createUser(user)
        return if (response.success) ResponseEntity.status(201).body(response)
               else ResponseEntity.badRequest().body(response)
    }

    // PUT /api/users/1
    // Body: { "name": "Ali Updated", "email": "ali@example.com", "age": 26 }
    // → updates user with id=1
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody user: User
    ): ResponseEntity<ApiResponse<User>> {
        val response = userService.updateUser(id, user)
        return if (response.success) ResponseEntity.ok(response)
               else ResponseEntity.badRequest().body(response)
    }

    // DELETE /api/users/1
    // → deletes user with id=1
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        val response = userService.deleteUser(id)
        return if (response.success) ResponseEntity.ok(response)
               else ResponseEntity.notFound().build()
    }

    // GET /api/users/search?name=ali
    // → search users by name
    @GetMapping("/search")
    fun searchUsers(@RequestParam name: String): ResponseEntity<ApiResponse<List<User>>> {
        return ResponseEntity.ok(userService.searchUsers(name))
    }
}
