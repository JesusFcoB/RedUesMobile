package com.example.reduesmobile.data.api

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

data class SeguidorResponse(
    val success: Boolean,
    val seguidoresCount: Int,
    val isFollowing: Boolean
)

interface SeguidoresApi {
    @POST("v1/usuarios/{id}/seguidores")
    suspend fun seguir(@Path("id") id: Int): Response<SeguidorResponse>

    @DELETE("v1/usuarios/{id}/seguidores")
    suspend fun dejarDeSeguir(@Path("id") id: Int): Response<SeguidorResponse>
}