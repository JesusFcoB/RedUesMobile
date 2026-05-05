package com.example.reduesmobile.data.api

import com.example.reduesmobile.data.dto.LikesResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface LikesApi {

    @POST("v1/publicaciones/{idPublicacion}/likes")
    suspend fun toggleLike(@Path("idPublicacion") idPublicacion: Int): Response<LikesResponse>
}