package com.example.reduesmobile.data.dto

import com.google.gson.annotations.SerializedName

data class ComentarioDto(
    @SerializedName("comentario_id") val id: Int,
    @SerializedName("publicacion_id") val publicacionId: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    val usuario: String,
    val texto: String,
    @SerializedName("fecha_comentario") val fechaComentario: String
)