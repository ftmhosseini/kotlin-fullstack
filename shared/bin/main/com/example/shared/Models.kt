package com.example.shared

// ─────────────────────────────────────────────────────────────────────────────
// SHARED MODELS
// These data classes are used by BOTH the Spring Boot server AND the Android app.
// This avoids duplicating code on both sides.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Represents a User in the system.
 * - Server uses this as a JPA entity (with @Entity annotation added in server module)
 * - Android uses this as a plain data class for Retrofit JSON parsing
 */
data class User(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val age: Int = 0
)

/**
 * Represents a Product.
 */
data class Product(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0
)

/**
 * Generic API response wrapper.
 * Every API response is wrapped in this so the Android app
 * always knows if it succeeded or not.
 *
 * Example:
 *   ApiResponse(success=true,  data=user,  message="User created")
 *   ApiResponse(success=false, data=null,  message="Email already exists")
 */
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String = "",
    val data: T? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// VALIDATION HELPERS (shared logic — works on both Android and server)
// ─────────────────────────────────────────────────────────────────────────────

object Validator {

    /** Returns an error message or null if valid */
    fun validateUser(user: User): String? {
        if (user.name.isBlank())        return "Name cannot be empty"
        if (user.name.length < 2)       return "Name must be at least 2 characters"
        if (!isValidEmail(user.email))  return "Invalid email address"
        if (user.age < 0 || user.age > 120) return "Age must be between 0 and 120"
        return null // null = valid ✅
    }

    fun validateProduct(product: Product): String? {
        if (product.name.isBlank())     return "Product name cannot be empty"
        if (product.price < 0)         return "Price cannot be negative"
        if (product.stock < 0)         return "Stock cannot be negative"
        return null
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
