package com.example.reduesmobile.data.dto

import com.google.gson.annotations.SerializedName

/*
*   "userName": "cuenta error",
  "email": "correo_sin_formato",
  "carrera": "Ingeniería en Software",
  "semestre": 20,
  "password": "123",
  "confirmPassword": "otra_cosa"
* */
data class RegisterRequest(
    @SerializedName("userName")
    val nombreUsuario: String,

    @SerializedName("email")
    val correo: String,

    val carrera: String,
    val semestre: Int,
    val password: String,
    val confirmPassword: String,
)
