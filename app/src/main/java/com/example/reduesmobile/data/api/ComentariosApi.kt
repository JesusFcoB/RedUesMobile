package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.ComentarioDto
import com.example.reduesmobile.data.dto.ComentariosRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ComentariosApi {
    @POST("v1/publicaciones/{publicacionId}/comentarios")
    suspend fun comentar(
        @Path("publicacionId") publicacionId: Int,
        @Body request: ComentariosRequestDto
    ): Response<ComentarioDto>

    @PUT("v1/publicaciones/comentarios/{id}")
    suspend fun editar(
        @Path("id") id: Int,
        @Body request: ComentariosRequestDto
    ): Response<ComentarioDto>

    @DELETE("v1/publicaciones/comentarios/{id}")
    suspend fun eliminar(
        @Path("id") id: Int
    ): Response<Unit>
}