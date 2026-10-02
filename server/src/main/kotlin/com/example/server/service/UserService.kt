package com.example.server.service

import com.example.server.model.UserEntity
import com.example.server.repository.UserRepository
import com.example.shared.ApiResponse
import com.example.shared.User
import com.example.shared.Validator
import org.springframework.stereotype.Service

// ─────────────────────────────────────────────────────────────────────────────
// SERVICE — contains all BUSINESS LOGIC for Users
//
// Separation of concerns:
//   Controller → receives HTTP request, calls Service
//   Service    → validates data, applies rules, calls Repository
//   Repository → talks to the database
//
// This way your Controller stays clean and your logic is testable.
// ─────────────────────────────────────────────────────────────────────────────

@Service
class UserService(
    private val userRepository: UserRepository  // injected automatically by Spring
) {

    // ── GET ALL ──────────────────────────────────────────────────────────────
    fun getAllUsers(): ApiResponse<List<User>> {
        val users = userRepository.findAll().map { it.toShared() }
        return ApiResponse(success = true, message = "Found ${users.size} users", data = users)
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    fun getUserById(id: Long): ApiResponse<User> {
        val user = userRepository.findById(id).orElse(null)
            ?: return ApiResponse(success = false, message = "User with id $id not found")
        return ApiResponse(success = true, data = user.toShared())
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    fun createUser(user: User): ApiResponse<User> {
        // 1. Validate using shared Validator (same code used in Android!)
        val error = Validator.validateUser(user)
        if (error != null) return ApiResponse(success = false, message = error)

        // 2. Check for duplicate email
        if (userRepository.findByEmail(user.email) != null) {
            return ApiResponse(success = false, message = "Email '${user.email}' already exists")
        }

        // 3. Save to database
        val saved = userRepository.save(user.toEntity())
        return ApiResponse(success = true, message = "User created successfully", data = saved.toShared())
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    fun updateUser(id: Long, user: User): ApiResponse<User> {
        // Check user exists
        if (!userRepository.existsById(id)) {
            return ApiResponse(success = false, message = "User with id $id not found")
        }

        val error = Validator.validateUser(user)
        if (error != null) return ApiResponse(success = false, message = error)

        val updated = userRepository.save(user.toEntity().copy(id = id))
        return ApiResponse(success = true, message = "User updated", data = updated.toShared())
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    fun deleteUser(id: Long): ApiResponse<Unit> {
        if (!userRepository.existsById(id)) {
            return ApiResponse(success = false, message = "User with id $id not found")
        }
        userRepository.deleteById(id)
        return ApiResponse(success = true, message = "User deleted")
    }

    // ── SEARCH ───────────────────────────────────────────────────────────────
    fun searchUsers(name: String): ApiResponse<List<User>> {
        val users = userRepository.findByNameContainingIgnoreCase(name).map { it.toShared() }
        return ApiResponse(success = true, data = users)
    }

    // ── MAPPERS (convert between Entity and Shared model) ────────────────────
    // Entity = database version (has @Entity, @Column etc.)
    // Shared = plain data class used by both server and Android

    private fun UserEntity.toShared() = User(id = id, name = name, email = email, age = age)
    private fun User.toEntity()       = UserEntity(id = id, name = name, email = email, age = age)
}
