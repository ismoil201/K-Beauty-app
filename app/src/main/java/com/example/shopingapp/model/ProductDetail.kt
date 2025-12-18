package com.example.shopingapp.model

data class ProductDetail(
    val id: Int,
    val name: String,
    val description: String,
    val brand: String?,
    val price: Double,
    val discountPrice: Double,
    val imageUrl: String,
    val category: String,
    val stock: Int,
    val favorite: Boolean
)
