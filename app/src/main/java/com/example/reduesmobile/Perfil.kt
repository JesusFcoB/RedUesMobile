package com.example.reduesmobile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.api.SeguidoresApi
import com.example.reduesmobile.data.api.UsuariosApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.ComentarioDto
import com.example.reduesmobile.data.dto.PublicacionDto
import com.example.reduesmobile.databinding.ActivityPerfilBinding
import com.example.reduesmobile.ui.ComentariosActivity
import com.example.reduesmobile.ui.EditarPerfilActivity
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
    private var usuarioActualId = -1
    private var perfilId = -1
    private var esPropioPerfil = false
    private var loSigo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioActualId = TokenManager(this).getUserId()
        // Si se pasa un ID externo lo usamos, si no cargamos el propio
        perfilId = intent.getIntExtra("usuarioId", usuarioActualId)
        esPropioPerfil = perfilId == usuarioActualId

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
            val intent = Intent(this, EditarPerfilActivity::class.java)
            intent.putExtra("bio", binding.txtUsuarioPerfil.text.toString())
            intent.putExtra("carrera", binding.txtCarrera.text.toString())
            intent.putExtra("semestre", binding.txtSemestre.text.toString()
                .replace("• ", "").replace(" Sem.", "").toIntOrNull() ?: 0)
            startActivityForResult(intent, 100)
        }

        binding.btnPublicacionesGuardadas.setOnClickListener {
            startActivity(Intent(this, GuardadosActivity::class.java))
        }
    }

    // Agregar este metodo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Recargar perfil con datos actualizados
            posts.clear()
            adapter.notifyDataSetChanged()
            cargarPerfil()
        }
    }
    private fun configurarRecycler() {
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
        binding.recyclerPerfil.layoutManager = LinearLayoutManager(this)
        binding.recyclerPerfil.adapter = adapter
    }

    private fun cargarPerfil() {
        lifecycleScope.launch {
            try {
                val usuariosApi = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(UsuariosApi::class.java)
                val publicacionesApi = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(PublicacionesApi::class.java)

                val perfilResponse = usuariosApi.getPerfil(perfilId)
                if (perfilResponse.isSuccessful) {
                    val perfil = perfilResponse.body()
                    perfil?.let {
                        binding.txtUsuarioPerfil.text = it.userName
                        binding.txtCarrera.text = it.carrera
                        binding.txtSemestre.text = "• ${it.semestre} Sem."
                        binding.contPublicaciones.text = it.cantidadPublicaciones.toString()
                        binding.contSeguidores.text = it.cantidadSeguidores.toString()
                        binding.contSeguidos.text = it.cantidadSiguiendo.toString()
                        loSigo = it.loSigo
                    }
                }

                // Mostrar/ocultar botones según si es propio perfil
                if (esPropioPerfil) {
                    binding.btnEditarPerfil.visibility = View.VISIBLE
                    binding.btnPublicacionesGuardadas.visibility = View.VISIBLE
                    binding.btnCerrarSesion.visibility = View.VISIBLE
                    binding.btnSeguir.visibility = View.GONE
                } else {
                    binding.btnEditarPerfil.visibility = View.GONE
                    binding.btnPublicacionesGuardadas.visibility = View.GONE
                    binding.btnCerrarSesion.visibility = View.GONE
                    binding.btnSeguir.visibility = View.VISIBLE
                    actualizarBotonSeguir()

                    binding.btnSeguir.setOnClickListener {
                        toggleSeguir()
                    }
                }

                val pubResponse = publicacionesApi.getPublicacionesUsuario(perfilId, 1, 10)
                if (pubResponse.isSuccessful) {
                    val nuevas = pubResponse.body() ?: emptyList()
                    adapter.agregarPosts(nuevas)
                }

            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarBotonSeguir() {
        binding.btnSeguir.text = if (loSigo) "Dejar de seguir" else "Seguir"
        binding.btnSeguir.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (loSigo) android.graphics.Color.GRAY
            else android.graphics.Color.parseColor("#ff8c00")
        )
    }

    private fun toggleSeguir() {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(SeguidoresApi::class.java)

                val response = if (loSigo) {
                    api.dejarDeSeguir(perfilId)
                } else {
                    api.seguir(perfilId)
                }

                if (response.isSuccessful) {
                    val resultado = response.body()
                    loSigo = resultado?.isFollowing ?: !loSigo
                    binding.contSeguidores.text = resultado?.seguidoresCount.toString()
                    actualizarBotonSeguir()
                } else {
                    Toast.makeText(this@Perfil, "Error al seguir", Toast.LENGTH_SHORT).show()
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
                if (nuevoContenido.isNotEmpty()) editarPost(post, nuevoContenido)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarPost(post: PublicacionDto, nuevoContenido: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@Perfil)
                    .create(PublicacionesApi::class.java)
                val response = api.editarPublicacion(
                    post.id,
                    CrearPublicacionRequest(contenido = nuevoContenido, tipo = post.tipo, autorId = usuarioActualId)
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
            .setMessage("¿Estás seguro?")
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
        binding.btnPerfil.setOnClickListener { }
    }
}