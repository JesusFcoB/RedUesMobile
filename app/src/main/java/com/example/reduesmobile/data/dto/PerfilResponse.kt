package com.example.reduesmobile.data.dto

import com.google.gson.annotations.SerializedName

data class PerfilResponse(
    val id: Int,
    val userName: String,
    val carrera: String,
    val semestre: Int,
    val bio: String,
    val fechaRegistro: String,
    val cantidadSeguidores: Int,
    val cantidadSiguiendo: Int,
    val cantidadPublicaciones: Int,
    val loSigo: Boolean
)
