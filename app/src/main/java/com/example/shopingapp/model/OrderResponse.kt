package com.example.shopingapp.model

data class OrderResponse(
    val id: Long,
    val status: String,
    val totalAmount: Double,
    val createdAt: String,
    val items: List<OrderItem>
)
