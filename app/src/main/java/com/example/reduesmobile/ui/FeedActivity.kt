package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.Apunte
import com.example.reduesmobile.Perfil
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.PublicacionDto
import com.example.reduesmobile.databinding.ActivityFeedBinding
import kotlinx.coroutines.launch

class FeedActivity : AppCompatActivity() {
    lateinit var binding: ActivityFeedBinding

    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<PublicacionDto>()
    private var page = 1
    private val pageSize = 10
    private var cargando = false
    private var finDePagina = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecycler()
        configurarNavegacion()
        cargarPublicaciones()
    }

    private fun configurarRecycler() {
        adapter = PostAdapter(
            posts,
            onLike = { post -> toggleLike(post) },
            onGuardar = { post -> toggleGuardar(post) },
            onComentar = { post ->
                // TODO: abrir pantalla de comentarios
                Toast.makeText(this, "Comentarios de ${post.autor}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerFeed.layoutManager = LinearLayoutManager(this)
        binding.recyclerFeed.adapter = adapter

        // Scroll infinito
        binding.recyclerFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val ultimoVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                if (!cargando && !finDePagina && ultimoVisible >= total - 3) {
                    cargarPublicaciones()
                }
            }
        })
    }

    private fun cargarPublicaciones() {
        if (cargando || finDePagina) return
        cargando = true
        binding.progressFeed.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@FeedActivity)
                    .create(PublicacionesApi::class.java)

                val response = api.getFeed(page, pageSize)

                if (response.isSuccessful) {
                    val nuevos = response.body() ?: emptyList()
                    if (nuevos.isEmpty()) {
                        finDePagina = true
                    } else {
                        adapter.agregarPosts(nuevos)
                        page++
                    }
                } else {
                    Toast.makeText(this@FeedActivity, "Error al cargar publicaciones", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FeedActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
                binding.progressFeed.visibility = View.GONE
            }
        }
    }

    private fun toggleLike(post: PublicacionDto) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@FeedActivity)
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
                Toast.makeText(this@FeedActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleGuardar(post: PublicacionDto) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@FeedActivity)
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
                Toast.makeText(this@FeedActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarNavegacion() {
        binding.btnCrearPublicacion.setOnClickListener {
            startActivity(Intent(this, Publicacion::class.java))
        }
        binding.btnCrearApunte.setOnClickListener {
            startActivity(Intent(this, Apunte::class.java))
        }
        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }
        binding.btnInicio.setOnClickListener {
            // Ya estamos en inicio
        }
    }
}