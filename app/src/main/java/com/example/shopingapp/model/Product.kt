package com.example.shopingapp.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Long = 0L,
    val name: String = "",
    val brand: String? = null,

    val price: Double = 0.0,

    @SerializedName("discountPrice")
    val discountPrice: Double = 0.0,

    val imageUrl: String? = null,
    val category: String = "",

    @SerializedName("favorite")
    var isFavorite: Boolean = false
)
