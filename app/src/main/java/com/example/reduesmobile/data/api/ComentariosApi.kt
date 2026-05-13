package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.ComentarioRequest
import com.example.reduesmobile.data.dto.ComentarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ComentariosApi {

    @GET("v1/publicaciones/{idPublicacion}/comentarios")
    suspend fun obtenerComentarios(@Path("idPublicacion") idPublicacion: Int): Response<List<ComentarioResponse>>

    @POST("v1/publicaciones/{idPublicacion}/comentarios")
    suspend fun comentar(@Path("idPublicacion") idPublicacion: Int,
                         @Body comentarioRequest: ComentarioRequest
    ): Response<Unit>

    @POST("v1/publicaciones/comentarios/{idComentario}")
    suspend fun editarComentario(@Path("idComentario") idComentario: Int,
                                 @Body comentarioRequest: ComentarioRequest): Response<Unit>

    @DELETE("v1/publicaciones/comentarios/{idComentario}")
    suspend fun eliminarComentario(@Path("idComentario") idComentario: Int): Response<Unit>
}
