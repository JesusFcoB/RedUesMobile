package com.example.reduesmobile.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.example.reduesmobile.data.api.GuardadosApi
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.databinding.ActivityGuardadosBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
class GuardadosActivity : AppCompatActivity() {
    lateinit var binding: ActivityGuardadosBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var actions: OnPostActionListenerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardadosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mostrarGuardados()

        binding.btnVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        NavigationHelper.setupBottomNavigation(this, binding.bottomNavigation)

    }

    private val editarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            postAdapter.refresh()
        }
    }
    private fun mostrarGuardados() {
        setupRecyclerView()

        actions = OnPostActionListenerImpl(
            context = this,
            rvPosts = binding.rvSavedPosts,
            postAdapter = postAdapter,
            scope = lifecycleScope,
            editarLauncher = editarLauncher  // <-- agregar
        )
        postAdapter.setListener(actions)

        setupPaging()
        setupRefreshLayout()
    }

    private val apiService by lazy {
        RetrofitInstance.Companion
            .getRetrofitInstance(this@GuardadosActivity)
            .create(GuardadosApi::class.java)
    }


    private fun setupRecyclerView() {
        postAdapter = PostAdapter(null, this)

        binding.rvSavedPosts.apply {
            layoutManager = LinearLayoutManager(this@GuardadosActivity)
            adapter = postAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupPaging() {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(null,apiService) }
        ).flow

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pager.collectLatest { pagingData ->
                    postAdapter.submitData(pagingData)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                postAdapter.loadStateFlow.collectLatest { loadStates ->
                    binding.swipeSavedRefresh.isRefreshing = loadStates.refresh is LoadState.Loading

                    val isListEmpty = loadStates.refresh is LoadState.NotLoading && postAdapter.itemCount == 0
                    binding.txtNoHayGuardados.visibility = if (isListEmpty) View.VISIBLE else View.GONE

                    val error = loadStates.refresh as? LoadState.Error
                    error?.let {
                        Toast.makeText(
                            this@GuardadosActivity,
                            "Error: ${it.error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupRefreshLayout() {
        val colorUes = ContextCompat.getColor(this, R.color.fondoLoginbtn)
        binding.swipeSavedRefresh.setColorSchemeColors(colorUes)
        binding.swipeSavedRefresh.setOnRefreshListener {
            postAdapter.refresh()
        }
    }
}