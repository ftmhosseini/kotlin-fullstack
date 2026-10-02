package com.example.server.controller

import com.example.server.service.ProductService
import com.example.shared.ApiResponse
import com.example.shared.Product
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    // GET /api/products
    @GetMapping
    fun getAllProducts() = ResponseEntity.ok(productService.getAllProducts())

    // GET /api/products/1
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ApiResponse<Product>> {
        val response = productService.getProductById(id)
        return if (response.success) ResponseEntity.ok(response)
               else ResponseEntity.notFound().build()
    }

    // POST /api/products
    @PostMapping
    fun createProduct(@RequestBody product: Product): ResponseEntity<ApiResponse<Product>> {
        val response = productService.createProduct(product)
        return if (response.success) ResponseEntity.status(201).body(response)
               else ResponseEntity.badRequest().body(response)
    }

    // PUT /api/products/1
    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody product: Product
    ): ResponseEntity<ApiResponse<Product>> {
        val response = productService.updateProduct(id, product)
        return if (response.success) ResponseEntity.ok(response)
               else ResponseEntity.badRequest().body(response)
    }

    // DELETE /api/products/1
    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        val response = productService.deleteProduct(id)
        return if (response.success) ResponseEntity.ok(response)
               else ResponseEntity.notFound().build()
    }

    // GET /api/products/search?name=phone
    @GetMapping("/search")
    fun searchProducts(@RequestParam name: String) =
        ResponseEntity.ok(productService.searchProducts(name))

    // GET /api/products/under-price?price=100
    @GetMapping("/under-price")
    fun getUnderPrice(@RequestParam price: Double) =
        ResponseEntity.ok(productService.getProductsUnderPrice(price))
}
