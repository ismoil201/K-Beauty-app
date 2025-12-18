package com.example.shopingapp.network

data class FavoriteRequest(
    val userId: Int,
    val productId: Int?
)