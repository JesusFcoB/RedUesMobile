package com.example.reduesmobile.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.AuthApi
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.dto.LoginRequest
import com.example.reduesmobile.data.dto.PublicacionRequest
import com.example.reduesmobile.databinding.ActivityPublicacionBinding
import kotlinx.coroutines.launch

class Publicacion : AppCompatActivity() {

    lateinit var binding: ActivityPublicacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NavigationHelper.setupBottomNavigation(this, binding.bottomNavigation)

        binding.btnAgregarPublicacion.setOnClickListener {
            val contenido = binding.etPublicacion.text?.toString()

            if (!contenido.isNullOrBlank()) {
                val request = PublicacionRequest(contenido, "Publicacion")
                publicar(request)
            } else {
                binding.etPublicacion.error = "La publicación no puede estar vacía"
                Toast.makeText(
                    this@Publicacion, "Por favor, escribe algo antes de publicar",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    private fun publicar(request: PublicacionRequest) {
        lifecycleScope.launch {
            try {
                binding.btnAgregarPublicacion.isEnabled = false

                val api = RetrofitInstance.Companion
                    .getRetrofitInstance(this@Publicacion)
                    .create(PublicacionesApi::class.java)

                val response = api.crearPublicacion(request)

                if (response.isSuccessful) {
                    binding.etPublicacion.text.clear()
                    binding.etPublicacion.clearFocus()
                    Toast.makeText(
                        this@Publicacion,
                        "Publicacion agregada exitosamente",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val errorBody = response.errorBody()?.string()

                    Toast.makeText(
                        this@Publicacion,
                        "Error al publicar: ${errorBody ?: "Datos incorrectos"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Publicacion, "Error de red: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            } finally {
                binding.btnAgregarPublicacion.isEnabled = true
            }
        }
    }
}