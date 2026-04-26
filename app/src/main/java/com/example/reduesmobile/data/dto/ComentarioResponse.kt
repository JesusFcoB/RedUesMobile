package com.example.reduesmobile.data.dto

import com.google.gson.annotations.SerializedName

data class ComentarioResponse(
    @SerializedName("comentario_id")
    val idComentario: Int,

    @SerializedName("publicacion_id")
    val idPublicacion: Int,

    @SerializedName("usuario_id")
    val idUsuario: Int,
    val usuario: String,
    val texto: String,

    @SerializedName("fecha_comentario")
    val fechaComentario: String
)
