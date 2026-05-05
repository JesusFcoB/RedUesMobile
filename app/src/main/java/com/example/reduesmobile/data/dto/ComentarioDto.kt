package com.example.reduesmobile.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComentarioDto(
    val comentario_id: Int,
    val publicacion_id: Int,
    val usuario_id: Int,
    val usuario: String,
    val texto: String,
    val fecha_comentario: String
) : Parcelable