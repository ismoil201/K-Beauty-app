package com.example.shopingapp.model

data class CartItem(
    val id: Long,
    val product: Product,
    var quantity: Int,
    var isSelected: Boolean = false
)
