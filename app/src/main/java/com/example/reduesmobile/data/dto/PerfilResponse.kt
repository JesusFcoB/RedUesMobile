package com.example.reduesmobile.data.dto

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
