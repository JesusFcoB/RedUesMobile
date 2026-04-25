package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.LoginRequest
import com.example.reduesmobile.data.dto.LoginResponse
import com.example.reduesmobile.data.dto.RegisterRequest
import com.example.reduesmobile.data.dto.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}