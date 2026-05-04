package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.Apunte
import com.example.reduesmobile.CrearPublicacionRequest
import com.example.reduesmobile.Perfil
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.databinding.ActivityPublicacionBinding
import kotlinx.coroutines.launch

class Publicacion : AppCompatActivity() {
    lateinit var binding: ActivityPublicacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarNavegacion()

        binding.btnAgregarApunte.setOnClickListener {
            val texto = binding.etApunte.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe algo antes de publicar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            publicar(texto)
        }
    }

    private fun publicar(contenido: String) {
        binding.btnAgregarApunte.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@Publicacion)
                    .create(PublicacionesApi::class.java)

                val response = api.crearPublicacion(
                    CrearPublicacionRequest(contenido = contenido, tipo = "Publicacion")
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@Publicacion, "Publicación creada", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Publicacion, FeedActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Publicacion, "Error al publicar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Publicacion, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnAgregarApunte.isEnabled = true
            }
        }
    }

    private fun configurarNavegacion() {
        binding.btnInicio.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        binding.btnCrearApunte.setOnClickListener {
            startActivity(Intent(this, Apunte::class.java))
            finish()
        }
        binding.btnCrearPublicacion.setOnClickListener {
            // Ya estamos aquí
        }
        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
            finish()
        }
    }
}