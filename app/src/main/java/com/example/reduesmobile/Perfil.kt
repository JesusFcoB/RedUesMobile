package com.example.reduesmobile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.api.UsuariosApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.ComentarioDto
import com.example.reduesmobile.data.dto.PublicacionDto
import com.example.reduesmobile.databinding.ActivityPerfilBinding
import com.example.reduesmobile.ui.ComentariosActivity
import com.example.reduesmobile.ui.FeedActivity
import com.example.reduesmobile.ui.GuardadosActivity
import com.example.reduesmobile.ui.MainActivity
import com.example.reduesmobile.ui.PostAdapter
import com.example.reduesmobile.ui.Publicacion
import kotlinx.coroutines.launch

class Perfil : AppCompatActivity() {
    lateinit var binding: ActivityPerfilBinding
    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<PublicacionDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecycler()
        configurarNavegacion()
        cargarPerfil()

        binding.btnCerrarSesion.setOnClickListener {
            TokenManager(this).deleteToken()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnEditarPerfil.setOnClickListener {
            Toast.makeText(this, "Editar perfil próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnPublicacionesGuardadas.setOnClickListener {
            startActivity(Intent(this, GuardadosActivity::class.java))
        }
    }

    private fun configurarRecycler() {
        val usuarioActualId = TokenManager(this).getUserId()

        adapter = PostAdapter(
            posts,
            currentUserId = usuarioActualId,
            onLike = { post -> toggleLike(post) },
            onGuardar = { post -> toggleGuardar(post) },
            onComentar = { post ->
                val intent = Intent(this, ComentariosActivity::class.java)
                intent.putExtra("publicacionId", post.id)
                // DESPUÉS
                intent.putParcelableArrayListExtra("comentarios", ArrayList(post.comentarios))
                startActivity(intent)
            },
            onEditar = { post -> mostrarDialogoEditar(post) },
            onEliminar = { post -> eliminarPost(post) }
        )

        binding.recyclerPerfil.layoutManager = LinearLayoutManager(this)
        binding.recyclerPerfil.adapter = adapter
    }

    private fun cargarPerfil() {
        lifecycleScope.launch {
            try {
                val usuarioId = TokenManager(this@Perfil).getUserId()
                if (usuarioId == -1) {
                    Toast.makeText(this@Perfil, "Sesión inválida", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val usuariosApi = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(UsuariosApi::class.java)
                val publicacionesApi = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(PublicacionesApi::class.java)

                val perfilResponse = usuariosApi.getPerfil(usuarioId)
                if (perfilResponse.isSuccessful) {
                    val perfil = perfilResponse.body()
                    perfil?.let {
                        binding.txtUsuarioPerfil.text = it.userName
                        binding.txtCarrera.text = it.carrera
                        binding.txtSemestre.text = "• ${it.semestre} Sem."
                        binding.contPublicaciones.text = it.cantidadPublicaciones.toString()
                        binding.contSeguidores.text = it.cantidadSeguidores.toString()
                        binding.contSeguidos.text = it.cantidadSiguiendo.toString()
                    }
                }

                val pubResponse = publicacionesApi.getPublicacionesUsuario(usuarioId, 1, 10)
                if (pubResponse.isSuccessful) {
                    val nuevas = pubResponse.body() ?: emptyList()
                    adapter.agregarPosts(nuevas)
                }

            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoEditar(post: PublicacionDto) {
        val input = android.widget.EditText(this)
        input.setText(post.contenido)

        AlertDialog.Builder(this)
            .setTitle("Editar publicación")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoContenido = input.text.toString().trim()
                if (nuevoContenido.isNotEmpty()) {
                    editarPost(post, nuevoContenido)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarPost(post: PublicacionDto, nuevoContenido: String) {
        lifecycleScope.launch {
            try {
                val usuarioId = TokenManager(this@Perfil).getUserId()
                val api = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(PublicacionesApi::class.java)

                val response = api.editarPublicacion(
                    post.id,
                    CrearPublicacionRequest(
                        contenido = nuevoContenido,
                        tipo = post.tipo,
                        autorId = usuarioId
                    )
                )

                if (response.isSuccessful) {
                    val index = posts.indexOfFirst { it.id == post.id }
                    if (index != -1) {
                        posts[index] = posts[index].copy(contenido = nuevoContenido)
                        adapter.notifyItemChanged(index)
                    }
                    Toast.makeText(this@Perfil, "Publicación editada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@Perfil, "Error al editar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarPost(post: PublicacionDto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar publicación")
            .setMessage("¿Estás seguro de eliminar esta publicación?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val api = RetrofitInstance.getRetrofitInstance(this@Perfil)
                            .create(PublicacionesApi::class.java)

                        val response = api.eliminarPublicacion(post.id)

                        if (response.isSuccessful) {
                            adapter.eliminarPost(post.id)
                            Toast.makeText(this@Perfil, "Publicación eliminada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@Perfil, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@Perfil, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toggleLike(post: PublicacionDto) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(PublicacionesApi::class.java)
                val response = api.toggleLike(post.id)
                if (response.isSuccessful) {
                    val resultado = response.body()
                    val index = posts.indexOfFirst { it.id == post.id }
                    if (index != -1) {
                        posts[index] = posts[index].copy(
                            cantidadLikes = resultado?.likes ?: post.cantidadLikes,
                            yaDioLike = resultado?.dioLike ?: !post.yaDioLike
                        )
                        adapter.notifyItemChanged(index)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleGuardar(post: PublicacionDto) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(PublicacionesApi::class.java)
                val response = api.toggleGuardado(post.id)
                if (response.isSuccessful) {
                    val resultado = response.body()
                    val index = posts.indexOfFirst { it.id == post.id }
                    if (index != -1) {
                        posts[index] = posts[index].copy(
                            cantidadGuardados = resultado?.guardados ?: post.cantidadGuardados,
                            yaGuardo = resultado?.guardado ?: !post.yaGuardo
                        )
                        adapter.notifyItemChanged(index)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
            startActivity(Intent(this, Publicacion::class.java))
            finish()
        }
        binding.btnPerfil.setOnClickListener {
            // Ya estamos aquí
        }
    }
}