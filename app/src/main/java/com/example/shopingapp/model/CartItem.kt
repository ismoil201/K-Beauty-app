package com.example.shopingapp.model

data class CartItem(
    val id: Long,
    val product: Product, // 🔥 yangi Product model
    var quantity: Int,
    var isSelected: Boolean = false
)
