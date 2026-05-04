package com.example.reduesmobile.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.ComentariosApi
import com.example.reduesmobile.data.dto.ComentarioDto
import com.example.reduesmobile.data.dto.ComentariosRequestDto
import com.example.reduesmobile.databinding.ActivityComentariosBinding
import kotlinx.coroutines.launch

class ComentariosActivity : AppCompatActivity() {
    lateinit var binding: ActivityComentariosBinding
    private lateinit var adapter: ComentariosAdapter
    private val comentarios = mutableListOf<ComentarioDto>()
    private var publicacionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos de la publicación
        publicacionId = intent.getIntExtra("publicacionId", -1)
        val comentariosIniciales = intent.getSerializableExtra("comentarios") as? ArrayList<ComentarioDto>
        comentariosIniciales?.let { comentarios.addAll(it) }

        configurarRecycler()

        binding.btnVolver.setOnClickListener { finish() }

        binding.btnEnviar.setOnClickListener {
            val texto = binding.etComentario.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener
            enviarComentario(texto)
        }
    }

    private fun configurarRecycler() {
        adapter = ComentariosAdapter(comentarios)
        binding.recyclerComentarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerComentarios.adapter = adapter
    }

    private fun enviarComentario(texto: String) {
        binding.btnEnviar.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@ComentariosActivity)
                    .create(ComentariosApi::class.java)

                val response = api.comentar(publicacionId, ComentariosRequestDto(texto))

                if (response.isSuccessful) {
                    val nuevo = response.body()
                    nuevo?.let {
                        adapter.agregar(it)
                        binding.recyclerComentarios.scrollToPosition(0)
                        binding.etComentario.text.clear()
                    }
                } else {
                    Toast.makeText(this@ComentariosActivity, "Error al comentar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ComentariosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnEnviar.isEnabled = true
            }
        }
    }
}