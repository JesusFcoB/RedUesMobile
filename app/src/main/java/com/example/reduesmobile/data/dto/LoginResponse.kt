package com.example.reduesmobile.data.dto

data class LoginResponse(
    val token: String,
    val tipo: String,
    val expiracion: String
)
