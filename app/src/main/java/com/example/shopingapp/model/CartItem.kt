package com.example.shopingapp.model

data class CartItem(
    val id: Int,
    val product: Product,
    var quantity: Int,
    var isSelected: Boolean = false
)
