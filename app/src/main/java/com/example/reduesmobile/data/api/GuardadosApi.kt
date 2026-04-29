package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.PublicacionResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GuardadosApi {

    @GET("v1/publicaciones/guardados")
    suspend fun obtenerGuardados(@Query("page") page: Int,
                                 @Query("pageSize") pageSize: Int
    ): List<PublicacionResponse>

    @POST("api/v1/publicaciones/{idPublicacion}/guardados")
    suspend fun toggleGuardado(@Path("idPublicacion") idPublicacion: Int)
}