package com.example.shopingapp.model

import Product

data class CartItem(
    val id: Int,
    val userId: Int,
    val product: Product,
    val quantity: Int
)
