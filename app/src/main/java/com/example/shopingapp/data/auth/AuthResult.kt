package com.example.shopingapp.data.auth

import com.example.shopingapp.model.LoginResponse

sealed class AuthResult {
    object Loading : AuthResult()
    object CodeSent : AuthResult()
    data class Verified(val user: LoginResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
