package com.example.reduesmobile.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.reduesmobile.data.api.GuardadosApi
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.dto.PublicacionResponse
import retrofit2.Response

class PostPagingSource(
    private val publicacionesApi: PublicacionesApi? = null,
    private val guardadosApi: GuardadosApi? = null,
    private val idUsuario: Int? = null,
): PagingSource<Int, PublicacionResponse>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PublicacionResponse> {
        val position = params.key ?: 1 // Empezamos en la página 1
        return try {
            // Llamada a la API
            val response = if (publicacionesApi != null) {
                if (idUsuario == null) {
                    publicacionesApi.obtenerPublicaciones(position,10)
                } else {
                    publicacionesApi.obtenerPublicacionesPorUsuario(idUsuario, position, 10)
                }
            } else {
                guardadosApi?.obtenerGuardados(position,10)
            }

            if (response!!.isSuccessful) {
                val posts = response.body() ?: emptyList()

                LoadResult.Page(
                    data = posts,
                    prevKey = if (position == 1) null else position - 1,
                    // Si la respuesta está vacía, no hay más páginas (null)
                    nextKey = if (posts.isEmpty()) null else position + 1
                )
            } else {
                LoadResult.Error(Exception("Error en el servidor: ${response.code()}"))
            }

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PublicacionResponse>): Int? = null
}