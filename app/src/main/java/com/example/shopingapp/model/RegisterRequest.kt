package com.example.shopingapp.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String? = null
)
