package com.example.reduesmobile.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reduesmobile.R
import com.example.reduesmobile.data.Carreras
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.AuthApi
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.api.UsuariosApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.PerfilResponse
import com.example.reduesmobile.databinding.ActivityFeedBinding
import com.example.reduesmobile.databinding.ActivityPerfilBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class Perfil : AppCompatActivity() {
    lateinit var binding: ActivityPerfilBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var actions: OnPostActionListenerImpl
    private var idPerfil: Int = 0
    private var loSigo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idUsuario = intent.getIntExtra("idUsuario", 0)
        llenarPerfil(idUsuario)

        setupRecyclerView()

        actions = OnPostActionListenerImpl(
            context = this,
            rvPosts = binding.rvUserPosts,
            postAdapter = postAdapter,
            scope = lifecycleScope
        )
        postAdapter.setListener(actions)

        setupPaging(idUsuario)
        setupRefreshLayout()

        binding.btnCerrarSesion.setOnClickListener {
            TokenManager(this).deleteToken()
            val logout = Intent(this, MainActivity::class.java)
            startActivity(logout)
            finish()
        }

        NavigationHelper.setupBottomNavigation(this, binding.bottomNavigation) {
            binding.rvUserPosts.scrollToPosition(0)
        }

        binding.btnPublicacionesGuardadas.setOnClickListener {
            val guardados = Intent(this, GuardadosActivity::class.java)
            startActivity(guardados)
        }
    }

    private fun llenarPerfil(idUsuario: Int) {
        lifecycleScope.launch {

            val perfil = obtenerPerfil(idUsuario)
            if (perfil != null) {
                binding.txtUsuarioPerfil.text = perfil.userName
                binding.txtCarrera.text = perfil.carrera
                binding.txtBio.text = perfil.bio
                binding.txtSemestre.text = "${perfil.semestre} semestre"
                binding.contSeguidos.text = perfil.cantidadSiguiendo.toString()
                binding.contSeguidores.text = perfil.cantidadSeguidores.toString()
                binding.contPublicaciones.text = perfil.cantidadPublicaciones.toString()
                idPerfil = perfil.id
                loSigo = perfil.loSigo
                configurarBotones()
            }
        }
    }

    private suspend fun obtenerPerfil(idUsuario: Int): PerfilResponse? {
        val api = RetrofitInstance.getRetrofitInstance(this)
                                                .create(UsuariosApi::class.java)

        val response = api.obtenerPerfil(idUsuario)

        if (!response.isSuccessful) {
            return null
        }
        return response.body()
    }


    private val apiService by lazy {
        RetrofitInstance.Companion
            .getRetrofitInstance(this@Perfil)
            .create(PublicacionesApi::class.java)
    }


    private fun setupRecyclerView() {
        postAdapter = PostAdapter(null)

        // Usamos binding para acceder al RecyclerView de tu activity_feed.xml
        binding.rvUserPosts.apply {
            layoutManager = LinearLayoutManager(this@Perfil)
            adapter = postAdapter
            // Opcional: optimización de rendimiento
            setHasFixedSize(true)
        }
    }

    private fun setupPaging(idUsuario: Int) {
        // 2. Configuramos el Pager
        val pager = Pager(
            config = PagingConfig(
                pageSize = 10,           // Cuántos items trae por página
                prefetchDistance = 3,    // Cuántos items antes del final dispara la carga
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(apiService,null, idUsuario) }
        ).flow

        // 3. Recolectamos el flujo de datos dentro de una Corrutina
        lifecycleScope.launch {
            // El repeatOnLifecycle es una buena práctica para no gastar recursos
            // cuando la app está en segundo plano
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pager.collectLatest { pagingData ->
                    postAdapter.submitData(pagingData)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // distinctUntilChangedBy evita que el código se ejecute si el estado de refresh no ha cambiado
                postAdapter.loadStateFlow
                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest { loadStates ->

                        // 1. Controlar la ruedita
                        binding.swipeUserRefresh.isRefreshing = loadStates.refresh is LoadState.Loading

                        // 2. Solo subir al inicio si el REFRESH terminó con éxito
                        // y no es una carga de "añadir más" (append)
                        if (loadStates.source.refresh is LoadState.NotLoading &&
                            loadStates.append is LoadState.NotLoading) {

                            binding.rvUserPosts.scrollToPosition(0)
                        }
                    }
            }
        }
    }

    private fun setupRefreshLayout() {
        val colorUes = ContextCompat.getColor(this, R.color.fondoLoginbtn)
        binding.swipeUserRefresh.setColorSchemeColors(colorUes)
        binding.swipeUserRefresh.setOnRefreshListener {
            postAdapter.refresh()
        }
    }

    private fun configurarBotones() {
        if (idPerfil != TokenManager(this@Perfil).getUserId()) {
            val colorSeguir = Color.GRAY
            val colorDejarSeguir = ContextCompat.getColor(this@Perfil, R.color.naranja_oscuro)

            binding.btnSeguir.text = if (loSigo) "Dejar de seguir" else "Seguir"
            binding.btnSeguir.backgroundTintList = ColorStateList.valueOf(
                if (loSigo) colorDejarSeguir else colorSeguir)

            binding.btnSeguir.visibility = View.VISIBLE
            binding.btnEditarPerfil.visibility = View.GONE
            binding.btnPublicacionesGuardadas.visibility = View.GONE
            binding.btnCerrarSesion.visibility = View.GONE
        } else {
            binding.btnSeguir.visibility = View.GONE
            binding.btnEditarPerfil.visibility = View.VISIBLE
            binding.btnPublicacionesGuardadas.visibility = View.VISIBLE
            binding.btnCerrarSesion.visibility = View.VISIBLE

        }
    }
}