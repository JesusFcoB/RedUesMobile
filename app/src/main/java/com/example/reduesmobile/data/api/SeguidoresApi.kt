package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.SeguidoresResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface SeguidoresApi {

    @POST("v1/usuarios/{idUsuario}/seguidores")
    suspend fun seguir(@Path("idUsuario") idUsuario: Int): Response<SeguidoresResponse>

    @DELETE("v1/usuarios/{idUsuario}/seguidores")
    suspend fun dejarDeSeguir(@Path("idUsuario") idUsuario: Int): Response<SeguidoresResponse>
}