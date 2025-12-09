package com.example.shopingapp.model
data class Order(
    val id: Int? = null,
    val user: User,
    val orderStatus: OrderStatus,
    val totalAmount: Double

)
