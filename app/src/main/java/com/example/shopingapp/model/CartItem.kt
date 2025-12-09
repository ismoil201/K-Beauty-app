package com.example.shopingapp.model


data class CartItem(
    val id: Int,
    val userId: Int,
    val product: Product,
    val quantity: Int
)
