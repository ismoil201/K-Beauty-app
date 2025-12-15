package com.example.shopingapp.model

data class AuthResponse(
    val token: String,
    val userId: Int,
    val fullName: String,
    val email: String
)
