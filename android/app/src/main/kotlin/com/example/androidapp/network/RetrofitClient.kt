package com.example.androidapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ─────────────────────────────────────────────────────────────────────────────
// RETROFIT CLIENT — singleton that creates the API service
//
// singleton = only ONE instance exists in the whole app (object keyword)
//
// BASE_URL: points to your Spring Boot server
//   - During development on emulator: use 10.0.2.2 (emulator's localhost)
//   - On real device on same WiFi: use your computer's local IP (192.168.x.x)
//   - In production: use your server's domain (https://api.myapp.com)
// ─────────────────────────────────────────────────────────────────────────────

object RetrofitClient {

    // 10.0.2.2 is Android emulator's way to reach host machine's localhost
    private const val BASE_URL = "http://10.0.2.2:8080"

    // Retrofit instance — built once, reused everywhere
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())  // JSON ↔ Kotlin
        .build()

    // The actual API service — call this to make HTTP requests
    val api: ApiService = retrofit.create(ApiService::class.java)
}
