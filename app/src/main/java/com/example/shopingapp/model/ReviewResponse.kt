package com.example.shopingapp.model

data class ReviewResponse(
    val id: Long,
    val userName: String,
    val rating: Int,        // 🔥 int bo‘lsin
    val content: String,
    val createdAt: String,
    val imageUrls: List<String> = emptyList()
)
