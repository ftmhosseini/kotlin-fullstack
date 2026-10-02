package com.example.server.service

import com.example.server.model.ProductEntity
import com.example.server.repository.ProductRepository
import com.example.shared.ApiResponse
import com.example.shared.Product
import com.example.shared.Validator
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    fun getAllProducts(): ApiResponse<List<Product>> {
        val products = productRepository.findAll().map { it.toShared() }
        return ApiResponse(success = true, message = "Found ${products.size} products", data = products)
    }

    fun getProductById(id: Long): ApiResponse<Product> {
        val product = productRepository.findById(id).orElse(null)
            ?: return ApiResponse(success = false, message = "Product with id $id not found")
        return ApiResponse(success = true, data = product.toShared())
    }

    fun createProduct(product: Product): ApiResponse<Product> {
        val error = Validator.validateProduct(product)
        if (error != null) return ApiResponse(success = false, message = error)

        val saved = productRepository.save(product.toEntity())
        return ApiResponse(success = true, message = "Product created", data = saved.toShared())
    }

    fun updateProduct(id: Long, product: Product): ApiResponse<Product> {
        if (!productRepository.existsById(id)) {
            return ApiResponse(success = false, message = "Product with id $id not found")
        }
        val error = Validator.validateProduct(product)
        if (error != null) return ApiResponse(success = false, message = error)

        val updated = productRepository.save(product.toEntity().copy(id = id))
        return ApiResponse(success = true, message = "Product updated", data = updated.toShared())
    }

    fun deleteProduct(id: Long): ApiResponse<Unit> {
        if (!productRepository.existsById(id)) {
            return ApiResponse(success = false, message = "Product with id $id not found")
        }
        productRepository.deleteById(id)
        return ApiResponse(success = true, message = "Product deleted")
    }

    fun searchProducts(name: String): ApiResponse<List<Product>> {
        val products = productRepository.findByNameContainingIgnoreCase(name).map { it.toShared() }
        return ApiResponse(success = true, data = products)
    }

    fun getProductsUnderPrice(price: Double): ApiResponse<List<Product>> {
        val products = productRepository.findByPriceLessThanEqual(price).map { it.toShared() }
        return ApiResponse(success = true, data = products)
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    private fun ProductEntity.toShared() = Product(id = id, name = name, description = description, price = price, stock = stock)
    private fun Product.toEntity()       = ProductEntity(id = id, name = name, description = description, price = price, stock = stock)
}
