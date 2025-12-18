package com.example.shopingapp.network

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)