package com.example.reduesmobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.databinding.ActivityApunteBinding
import com.example.reduesmobile.ui.FeedActivity
import com.example.reduesmobile.ui.Publicacion
import kotlinx.coroutines.launch

class Apunte : AppCompatActivity() {
    lateinit var binding: ActivityApunteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApunteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarNavegacion()

        binding.btnAgregarApunte.setOnClickListener {
            val texto = binding.etApunte.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe algo antes de publicar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            publicarApunte(texto)
        }
    }

    private fun publicarApunte(contenido: String) {
        binding.btnAgregarApunte.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@Apunte)
                    .create(PublicacionesApi::class.java)

                val response = api.crearPublicacion(
                    CrearPublicacionRequest(contenido = contenido, tipo = "Apunte")
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@Apunte, "Apunte publicado", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Apunte, FeedActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Apunte, "Error al publicar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Apunte, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
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
        binding.btnCrearPublicacion.setOnClickListener {
            startActivity(Intent(this, Publicacion::class.java))
            finish()
        }
        binding.btnCrearApunte.setOnClickListener {
            // Ya estamos aquí
        }
        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
            finish()
        }
    }
}