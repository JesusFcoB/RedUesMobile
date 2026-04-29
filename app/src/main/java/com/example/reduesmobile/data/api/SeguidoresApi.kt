package com.example.reduesmobile.data.api

import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface SeguidoresApi {

    @POST("api/v1/usuarios/{idUsuario}/seguidores")
    suspend fun seguir(@Path("idUsuario") idUsuario: Int)

    @DELETE("api/v1/usuarios/{idUsuario}/seguidores")
    suspend fun dejarDeSeguir(@Path("idUsuario") idUsuario: Int)
}