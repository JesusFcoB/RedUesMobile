package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.PerfilDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuariosApi {
    @GET("v1/usuarios/perfiles/{id}")
    suspend fun getPerfil(@Path("id") id: Int): Response<PerfilDto>

    @GET("v1/usuarios")
    suspend fun buscarUsuarios(
        @Query("usuario") usuario: String,
        @Query("filtro") filtro: String = "relevancia"
    ): Response<List<PerfilDto>>
}