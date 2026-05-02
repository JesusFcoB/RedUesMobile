package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
import com.example.reduesmobile.data.api.AuthApi
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.databinding.ActivityFeedBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class FeedActivity : AppCompatActivity() {
    lateinit var binding: ActivityFeedBinding
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupPaging()
        setupRefreshLayout()

        binding.perfilboton.setOnClickListener {
            TokenManager(this).deleteToken()
            val logout = Intent(this, MainActivity::class.java)
            startActivity(logout)
            finish()
        }
    }

    private val apiService by lazy {
        RetrofitInstance.Companion
            .getRetrofitInstance(this@FeedActivity)
            .create(PublicacionesApi::class.java)
    }


    private fun setupRecyclerView() {
        postAdapter = PostAdapter()

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
        val colorUes = android.graphics.Color.parseColor("#ff7400")
        binding.swipeRefresh.setColorSchemeColors(colorUes)
        binding.swipeRefresh.setOnRefreshListener {
            postAdapter.refresh()
        }
    }
}