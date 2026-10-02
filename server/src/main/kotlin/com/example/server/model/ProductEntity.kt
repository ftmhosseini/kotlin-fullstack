package com.example.server.model

import jakarta.persistence.*

@Entity
@Table(name = "products")
data class ProductEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(length = 1000)   // allow longer text for description
    val description: String = "",

    @Column(nullable = false)
    val price: Double = 0.0,

    @Column(nullable = false)
    val stock: Int = 0
)
