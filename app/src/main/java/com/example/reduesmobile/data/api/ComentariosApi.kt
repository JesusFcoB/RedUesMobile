package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.ComentarioRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface ComentariosApi {

    @POST("api/v1/publicaciones/{idPublicacion}/comentarios")
    suspend fun comentar(@Path("idPublicacion") idPublicacion: Int,
                         @Body comentarioRequest: ComentarioRequest
    )

    @POST("api/v1/publicaciones/comentarios/{idComentario}")
    suspend fun editarComentario(@Path("idComentario") idComentario: Int,
                                 @Body comentarioRequest: ComentarioRequest)

    @DELETE("api/v1/publicaciones/comentarios/{idComentario}")
    suspend fun eliminarComentario(@Path("idComentario") idComentario: Int)
}