package com.example.reduesmobile.data.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val usuario: String,
    @SerializedName("Password") val password: String
)
