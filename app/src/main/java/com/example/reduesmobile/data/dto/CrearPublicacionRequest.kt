package com.example.reduesmobile

data class CrearPublicacionRequest(
    val contenido: String,
    val tipo: String,
    val autorId: Int = 0  // ← agregar este campo
)