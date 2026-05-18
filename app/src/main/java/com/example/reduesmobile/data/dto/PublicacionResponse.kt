package com.example.reduesmobile.data.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PublicacionResponse(
    @SerializedName("publicacion_id")
    val idPublicacion: Int,

    @SerializedName("autor_id")
    val idAutor: Int,
    val autor: String,
    var contenido: String, // Cambiado a var para actualización instantánea
    val tipo: String,

    @SerializedName("fecha_creacion")
    val fechaCreacion: String,
    val comentarios: List<ComentarioResponse> = emptyList(),
    var cantidadLikes: Int,
    var yaDioLike: Boolean,
    var cantidadGuardados: Int,
    var yaGuardo: Boolean
) : Serializable
