package com.example.shopingapp.network

data class AuthResponse(
    val token: String,
    val userId: Int,
    val fullName: String,
    val email: String
)