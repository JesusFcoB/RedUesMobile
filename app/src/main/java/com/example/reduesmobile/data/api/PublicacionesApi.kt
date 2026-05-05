package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.PublicacionRequest
import com.example.reduesmobile.data.dto.PublicacionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PublicacionesApi {

    @GET("v1/publicaciones")
    suspend fun obtenerPublicaciones(@Query("page") page: Int,
                                     @Query("pageSize") pageSize: Int
    ): Response<List<PublicacionResponse>>


    @GET("v1/publicaciones/usuarios/{id}")
    suspend fun obtenerPublicacionesPorUsuario(@Path("id") usuarioId: Int,
                                               @Query("page") page: Int,
                                               @Query("pageSize") pageSize: Int
    ): Response<List<PublicacionResponse>>

    @POST("v1/publicaciones")
    suspend fun crearPublicacion(@Body publicacionRequest: PublicacionRequest)

    @POST("v1/publicaciones/apuntes")
    suspend fun crearApunte(@Body publicacionRequest: PublicacionRequest)

    @PUT("v1/publicaciones")
    suspend fun editarPublicacion(@Body publicacionRequest: PublicacionRequest)

    @DELETE("v1/publicaciones/{id}")
    suspend fun eliminarPulicacion(@Path("id") idpublicacion: Int)
}