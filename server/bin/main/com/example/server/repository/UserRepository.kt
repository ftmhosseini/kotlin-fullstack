package com.example.server.repository

import com.example.server.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

// ─────────────────────────────────────────────────────────────────────────────
// REPOSITORY — handles ALL database operations for Users
//
// JpaRepository<UserEntity, Long> gives you these for FREE:
//   findAll()       → SELECT * FROM users
//   findById(id)    → SELECT * FROM users WHERE id = ?
//   save(user)      → INSERT or UPDATE
//   deleteById(id)  → DELETE FROM users WHERE id = ?
//   count()         → SELECT COUNT(*) FROM users
//
// You don't write any SQL! Spring handles it.
// ─────────────────────────────────────────────────────────────────────────────

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {

    // Custom query — Spring generates the SQL from the method name!
    // "find By Email" → SELECT * FROM users WHERE email = ?
    fun findByEmail(email: String): UserEntity?

    // SELECT * FROM users WHERE name LIKE %keyword%
    fun findByNameContainingIgnoreCase(name: String): List<UserEntity>
}
