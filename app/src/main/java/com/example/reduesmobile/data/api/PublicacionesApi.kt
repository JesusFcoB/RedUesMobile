package com.example.reduesmobile.data.api


import com.example.reduesmobile.data.dto.PublicacionDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.reduesmobile.CrearPublicacionRequest
import retrofit2.http.Body


data class LikeResponse(val likes: Int, val dioLike: Boolean)
data class GuardadoResponse(val guardados: Int, val guardado: Boolean)

interface PublicacionesApi {
    @GET("v1/publicaciones")
    suspend fun getFeed(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<PublicacionDto>>

    @POST("v1/publicaciones/{id}/likes")
    suspend fun toggleLike(
        @Path("id") id: Int
    ): Response<LikeResponse>

    @POST("v1/publicaciones/{id}/guardados")
    suspend fun toggleGuardado(
        @Path("id") id: Int
    ): Response<GuardadoResponse>

    @POST("v1/publicaciones")
    suspend fun crearPublicacion(
        @Body request: CrearPublicacionRequest
    ): Response<Unit>

    @GET("v1/usuarios/{id}/publicaciones")
    suspend fun getPublicacionesUsuario(
        @Path("id") id: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<PublicacionDto>>
}

