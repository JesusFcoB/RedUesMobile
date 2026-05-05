package com.example.reduesmobile.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.Apunte
import com.example.reduesmobile.CrearPublicacionRequest
import com.example.reduesmobile.Perfil
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.ComentarioDto
import com.example.reduesmobile.data.dto.PublicacionDto
import com.example.reduesmobile.databinding.ActivityGuardadosBinding
import kotlinx.coroutines.launch

class GuardadosActivity : AppCompatActivity() {
    lateinit var binding: ActivityGuardadosBinding
    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<PublicacionDto>()
    private var page = 1
    private val pageSize = 10
    private var cargando = false
    private var finDePagina = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecycler()
        configurarNavegacion()
        cargarGuardados()

        binding.btnVolver.setOnClickListener { finish() }
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
                intent.putParcelableArrayListExtra("comentarios", ArrayList(post.comentarios))
                startActivity(intent)
            },
            onEditar = { post -> mostrarDialogoEditar(post) },
            onEliminar = { post -> eliminarPost(post) }
        )

        binding.recyclerGuardados.layoutManager = LinearLayoutManager(this)
        binding.recyclerGuardados.adapter = adapter

        // Scroll infinito
        binding.recyclerGuardados.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val ultimoVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount
                if (!cargando && !finDePagina && ultimoVisible >= total - 3) {
                    cargarGuardados()
                }
            }
        })
    }

    private fun cargarGuardados() {
        if (cargando || finDePagina) return
        cargando = true
        binding.progressGuardados.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@GuardadosActivity)
                    .create(PublicacionesApi::class.java)

                val response = api.getGuardados(page, pageSize)

                if (response.isSuccessful) {
                    val nuevos = response.body() ?: emptyList()
                    if (nuevos.isEmpty()) {
                        finDePagina = true
                        if (page == 1) {
                            Toast.makeText(this@GuardadosActivity, "No tienes publicaciones guardadas", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        adapter.agregarPosts(nuevos)
                        page++
                    }
                } else {
                    Toast.makeText(this@GuardadosActivity, "Error al cargar guardados", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GuardadosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
                binding.progressGuardados.visibility = View.GONE
            }
        }
    }

    private fun toggleLike(post: PublicacionDto) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@GuardadosActivity)
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
                Toast.makeText(this@GuardadosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleGuardar(post: PublicacionDto) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@GuardadosActivity)
                    .create(PublicacionesApi::class.java)
                val response = api.toggleGuardado(post.id)
                if (response.isSuccessful) {
                    // Si quita el guardado, remover de la lista
                    val resultado = response.body()
                    if (resultado?.guardado == false) {
                        adapter.eliminarPost(post.id)
                    } else {
                        val index = posts.indexOfFirst { it.id == post.id }
                        if (index != -1) {
                            posts[index] = posts[index].copy(
                                cantidadGuardados = resultado?.guardados ?: post.cantidadGuardados,
                                yaGuardo = resultado?.guardado ?: !post.yaGuardo
                            )
                            adapter.notifyItemChanged(index)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@GuardadosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoEditar(post: PublicacionDto) {
        val input = EditText(this)
        input.setText(post.contenido)

        AlertDialog.Builder(this)
            .setTitle("Editar publicación")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoContenido = input.text.toString().trim()
                if (nuevoContenido.isNotEmpty()) editarPost(post, nuevoContenido)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarPost(post: PublicacionDto, nuevoContenido: String) {
        lifecycleScope.launch {
            try {
                val usuarioId = TokenManager(this@GuardadosActivity).getUserId()
                val api = RetrofitInstance.getRetrofitInstance(this@GuardadosActivity)
                    .create(PublicacionesApi::class.java)

                val response = api.editarPublicacion(
                    post.id,
                    CrearPublicacionRequest(contenido = nuevoContenido, tipo = post.tipo, autorId = usuarioId)
                )

                if (response.isSuccessful) {
                    val index = posts.indexOfFirst { it.id == post.id }
                    if (index != -1) {
                        posts[index] = posts[index].copy(contenido = nuevoContenido)
                        adapter.notifyItemChanged(index)
                    }
                    Toast.makeText(this@GuardadosActivity, "Publicación editada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@GuardadosActivity, "Error al editar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GuardadosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarPost(post: PublicacionDto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar publicación")
            .setMessage("¿Estás seguro?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val api = RetrofitInstance.getRetrofitInstance(this@GuardadosActivity)
                            .create(PublicacionesApi::class.java)
                        val response = api.eliminarPublicacion(post.id)
                        if (response.isSuccessful) {
                            adapter.eliminarPost(post.id)
                            Toast.makeText(this@GuardadosActivity, "Publicación eliminada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@GuardadosActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@GuardadosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
            startActivity(Intent(this, Perfil::class.java))
            finish()
        }
    }
}