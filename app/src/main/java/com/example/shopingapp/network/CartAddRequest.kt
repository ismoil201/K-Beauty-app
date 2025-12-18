package com.example.shopingapp.network

data class CartAddRequest(
    val userId: Int,
    val productId: Int,
    val quantity: Int
)