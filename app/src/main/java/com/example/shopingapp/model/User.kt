package com.example.shopingapp.model
data class User(
    val id: Int,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val profileImage: String?,
    val role: String
)
