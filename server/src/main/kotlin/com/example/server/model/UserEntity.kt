package com.example.server.model

import jakarta.persistence.*

// ─────────────────────────────────────────────────────────────────────────────
// JPA ENTITY — maps this Kotlin class to a database table
//
// @Entity       → this class = a database table
// @Table        → name of the table in the database
// @Id           → this field is the primary key
// @GeneratedValue → database auto-generates the ID (1, 2, 3...)
// @Column       → maps field to a database column
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(name = "users")
data class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false, unique = true)  // unique = no duplicate emails
    val email: String = "",

    @Column(nullable = false)
    val age: Int = 0
)
