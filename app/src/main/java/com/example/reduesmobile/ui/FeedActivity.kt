package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.api.UsuariosApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.databinding.ActivityFeedBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class FeedActivity : AppCompatActivity() {
    lateinit var binding: ActivityFeedBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var actions: OnPostActionListenerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupProfileRecycler()


        actions = OnPostActionListenerImpl(
            context = this,
            rvPosts = binding.rvPosts,
            postAdapter = postAdapter,
            scope = lifecycleScope
        )
        postAdapter.setListener(actions)

        setupPaging()
        setupRefreshLayout()

        NavigationHelper.setupBottomNavigation(this, binding.bottomNavigation) {
            binding.rvPosts.scrollToPosition(0)
        }
        binding.btnAccionBusqueda.setOnClickListener {
            val username = binding.buscar.query?.toString()?.trim().orEmpty()
            if (username.isNotBlank()) {
                searchAnimationIn()
                loadProfiles(username)
            } else {
                Toast.makeText(this, "Ingresa un usuario para buscar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            searchAnimationOut()
        }

        onBackPressedDispatcher.addCallback(this, backCallback)

    }

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            if (binding.cardSearchResults.visibility == View.VISIBLE) {
                searchAnimationOut()
            } else {
                finish()
            }
        }
    }

    private val apiService by lazy {
        RetrofitInstance.Companion
            .getRetrofitInstance(this@FeedActivity)
            .create(PublicacionesApi::class.java)
    }


    private fun setupRecyclerView() {
        postAdapter = PostAdapter(null)

        // Usamos binding para acceder al RecyclerView de tu activity_feed.xml
        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(this@FeedActivity)
            adapter = postAdapter
            // Opcional: optimización de rendimiento
            setHasFixedSize(true)
        }
    }

    private fun setupPaging() {
        // 2. Configuramos el Pager
        val pager = Pager(
            config = PagingConfig(
                pageSize = 10,           // Cuántos items trae por página
                prefetchDistance = 3,    // Cuántos items antes del final dispara la carga
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(apiService) }
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
                        binding.swipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading

                        // 2. Solo subir al inicio si el REFRESH terminó con éxito
                        // y no es una carga de "añadir más" (append)
                        if (loadStates.source.refresh is LoadState.NotLoading &&
                            loadStates.append is LoadState.NotLoading) {

                            binding.rvPosts.scrollToPosition(0)
                        }
                    }
            }
        }
    }

    private fun setupRefreshLayout() {
        val colorUes = ContextCompat.getColor(this, R.color.fondoLoginbtn)
        binding.swipeRefresh.setColorSchemeColors(colorUes)
        binding.swipeRefresh.setOnRefreshListener {
            postAdapter.refresh()
        }
    }

    private fun setupProfileRecycler() {
        profileAdapter = ProfileAdapter { perfil ->
            val intent = Intent(this, Perfil::class.java)
            intent.putExtra("idUsuario", perfil.id)
            startActivity(intent)
        }

        binding.rvUserSearch.apply {
            layoutManager = LinearLayoutManager(this@FeedActivity)
            adapter = profileAdapter
        }
    }

    private fun loadProfiles(username: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance
                    .getRetrofitInstance(this@FeedActivity)
                    .create(UsuariosApi::class.java)

                val response = api.buscarPerfil(username, null)

                if (response.isSuccessful) {
                    val list = response.body().orEmpty()

                    profileAdapter.submitList(list)

                    binding.cardSearchResults.visibility = View.VISIBLE

                    if (list.isEmpty()) {
                        binding.txtResultadoBusqueda.text = "No hay resultados"
                        binding.rvUserSearch.visibility = View.GONE
                    } else {
                        binding.txtResultadoBusqueda.text = "Resultados"
                        binding.rvUserSearch.visibility = View.VISIBLE
                    }

                }
            } catch (e: Exception) {
                Toast.makeText(this@FeedActivity, "Error cargando perfiles", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchAnimationIn() {
        binding.viewBlurOverlay.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(300).start()
        }

        binding.cardSearchResults.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = -50f

            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start()
        }
    }

    private fun searchAnimationOut() {
        binding.viewBlurOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { binding.viewBlurOverlay.visibility = View.GONE }
            .start()

        binding.cardSearchResults.animate()
            .alpha(0f)
            .translationY(-50f)
            .setDuration(300)
            .withEndAction {
                binding.cardSearchResults.visibility = View.GONE
                profileAdapter.submitList(emptyList())
            }
            .start()
    }
}