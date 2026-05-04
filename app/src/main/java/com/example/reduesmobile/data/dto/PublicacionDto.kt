package com.example.reduesmobile.data.dto

import com.google.gson.annotations.SerializedName

data class PublicacionDto(
    @SerializedName("publicacion_id") val id: Int,
    @SerializedName("autor_id") val autorId: Int,
    val autor: String,
    val contenido: String,
    val tipo: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    val comentarios: List<ComentarioDto>,
    val cantidadLikes: Int,
    val yaDioLike: Boolean,
    val cantidadGuardados: Int,
    val yaGuardo: Boolean
)