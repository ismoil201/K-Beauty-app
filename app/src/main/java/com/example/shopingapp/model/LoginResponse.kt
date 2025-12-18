package com.example.shopingapp.model

data class LoginResponse(
    val id: Int,
    val email: String,
    val fullName: String?,
    val token: String
)
