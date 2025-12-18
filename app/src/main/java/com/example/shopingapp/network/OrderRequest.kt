package com.example.shopingapp.network

data class OrderRequest(
    val userId: Int,
    val address: String,
    val totalAmount: Double
)