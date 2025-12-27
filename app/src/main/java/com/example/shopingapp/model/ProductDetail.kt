package com.example.shopingapp.model

data class ProductDetail(
    val id: Long,
    val name: String,
    val description: String,
    val brand: String?,
    val price: Double,
    val discountPrice: Double,
    val category: String,
    val stock: Int,
    val favorite: Boolean,

    // 🔥 KO‘P RASM
    val images: List<ProductImage>
)
