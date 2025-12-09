package com.example.shopingapp.model
data class CartAddRequest(
    val userId: Int,
    val productId: Int,
    val quantity: Int
)
