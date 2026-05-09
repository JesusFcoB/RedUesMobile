package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.PerfilRequest
import com.example.reduesmobile.data.dto.PerfilResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuariosApi {

    @GET("v1/usuarios/perfiles/{idUsuario}")
    suspend fun obtenerPerfil(@Path("idUsuario") idUsuario: Int): Response<PerfilResponse>

    @GET("v1/usuarios")
    suspend fun buscarPerfil(@Query("usuario") nombreUsuario: String,
                             @Query("filtro") filtro: String?): Response<List<PerfilResponse>>

    @PUT("v1/usuarios/perfiles")
    suspend fun editarPerfil(@Body perfilRequest: PerfilRequest): Response<Unit>
}