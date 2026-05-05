package com.example.reduesmobile.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.ComentariosApi
import com.example.reduesmobile.data.auth.TokenManager
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

        publicacionId = intent.getIntExtra("publicacionId", -1)
        // DESPUÉS
        val comentariosIniciales = intent.getParcelableArrayListExtra<ComentarioDto>("comentarios")
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
        val usuarioActualId = TokenManager(this).getUserId()

        adapter = ComentariosAdapter(
            comentarios,
            currentUserId = usuarioActualId,
            onEditar = { comentario -> mostrarDialogoEditar(comentario) },
            onEliminar = { comentario -> eliminarComentario(comentario) }
        )
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

    private fun mostrarDialogoEditar(comentario: ComentarioDto) {
        val input = EditText(this)
        input.setText(comentario.texto)

        AlertDialog.Builder(this)
            .setTitle("Editar comentario")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoTexto = input.text.toString().trim()
                if (nuevoTexto.isNotEmpty()) {
                    editarComentario(comentario, nuevoTexto)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarComentario(comentario: ComentarioDto, nuevoTexto: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@ComentariosActivity)
                    .create(ComentariosApi::class.java)

                val response = api.editar(comentario.comentario_id, ComentariosRequestDto(nuevoTexto))

                if (response.isSuccessful) {
                    val actualizado = response.body()
                    actualizado?.let { adapter.actualizar(it) }
                    Toast.makeText(this@ComentariosActivity, "Comentario editado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ComentariosActivity, "Error al editar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ComentariosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarComentario(comentario: ComentarioDto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar comentario")
            .setMessage("¿Estás seguro de eliminar este comentario?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val api = RetrofitInstance.getRetrofitInstance(this@ComentariosActivity)
                            .create(ComentariosApi::class.java)

                        val response = api.eliminar(comentario.comentario_id)

                        if (response.isSuccessful) {
                            adapter.eliminar(comentario.comentario_id)
                            Toast.makeText(this@ComentariosActivity, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ComentariosActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ComentariosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}