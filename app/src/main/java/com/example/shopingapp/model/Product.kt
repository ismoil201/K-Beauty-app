package com.example.shopingapp.model

import com.google.gson.annotations.SerializedName



data class Product(
    val id: Long,
    val name: String,
    val brand: String? = null,
    val price: Double,
    val discountPrice: Double,
    val category: String,

    @SerializedName("favorite")
    var isFavorite: Boolean,

    val images: List<ProductImage>
) {
    fun mainImage(): String? =
        images.firstOrNull { it.main }?.imageUrl
            ?: images.firstOrNull()?.imageUrl
}

