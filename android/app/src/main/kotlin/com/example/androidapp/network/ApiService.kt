package com.example.androidapp.network

import com.example.androidapp.model.ApiResponse
import com.example.androidapp.model.Product
import com.example.androidapp.model.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Users ─────────────────────────────────────────────────────────────────
    @GET("/api/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<User>>>

    @GET("/api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<ApiResponse<User>>

    @POST("/api/users")
    suspend fun createUser(@Body user: User): Response<ApiResponse<User>>

    @PUT("/api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body user: User
    ): Response<ApiResponse<User>>

    @DELETE("/api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @GET("/api/users/search")
    suspend fun searchUsers(@Query("name") name: String): Response<ApiResponse<List<User>>>

    // ── Products ──────────────────────────────────────────────────────────────
    @GET("/api/products")
    suspend fun getAllProducts(): Response<ApiResponse<List<Product>>>

    @POST("/api/products")
    suspend fun createProduct(@Body product: Product): Response<ApiResponse<Product>>

    @DELETE("/api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @GET("/api/products/search")
    suspend fun searchProducts(@Query("name") name: String): Response<ApiResponse<List<Product>>>
}
