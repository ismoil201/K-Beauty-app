package com.example.shopingapp.model

data class Product(
    val id: Int? = null,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String
)
