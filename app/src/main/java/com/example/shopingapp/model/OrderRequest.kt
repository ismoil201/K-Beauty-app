package com.example.shopingapp.model
data class OrderRequest(
    val userId: Int,
    val address: String,
    val totalAmount: Int
)
