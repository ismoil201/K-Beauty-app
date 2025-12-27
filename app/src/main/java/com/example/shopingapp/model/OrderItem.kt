package com.example.shopingapp.model

data class OrderItem(
    val productId: Long,
    val name: String,
    val imageUrl: String,
    val price: Double,
    val quantity: Int
)
