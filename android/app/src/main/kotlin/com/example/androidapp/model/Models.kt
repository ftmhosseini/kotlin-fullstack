package com.example.androidapp.model

// ─────────────────────────────────────────────────────────────────────────────
// LOCAL MODELS — mirrors the shared module on the server side.
// In a production app you'd use Kotlin Multiplatform to share these.
// For now, they are duplicated here to keep the Android project self-contained.
// ─────────────────────────────────────────────────────────────────────────────

data class User(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val age: Int = 0
)

data class Product(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0
)

data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String = "",
    val data: T? = null
)
