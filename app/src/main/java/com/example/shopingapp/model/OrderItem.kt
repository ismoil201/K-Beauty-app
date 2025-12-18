package com.example.shopingapp.model

data class OrderItem(
    val productId: Int,
    val productName: String,
    val imageUrl: String,
    val price: Double,
    val quantity: Int
)
