package com.example.shopingapp.model

data class CartAddRequest(
    val productId: Long,
    val quantity: Int
)
