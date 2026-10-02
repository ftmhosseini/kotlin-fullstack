package com.example.server.repository

import com.example.server.model.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<ProductEntity, Long> {

    // Find products by name (case-insensitive search)
    fun findByNameContainingIgnoreCase(name: String): List<ProductEntity>

    // Find all products with price less than a value
    fun findByPriceLessThanEqual(price: Double): List<ProductEntity>

    // Find products that are in stock
    fun findByStockGreaterThan(stock: Int): List<ProductEntity>
}
