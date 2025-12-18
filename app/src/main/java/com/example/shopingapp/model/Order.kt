package com.example.shopingapp.model
data class Order(
    val id: Int,
    val status: String,
    val totalAmount: Double,
    val createdAt: String,
    val items: List<OrderItem>
)
