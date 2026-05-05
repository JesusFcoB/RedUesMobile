package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.example.reduesmobile.R
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reduesmobile.Apunte
import com.example.reduesmobile.Perfil
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.UsuariosApi
import com.example.reduesmobile.data.dto.PerfilDto
import com.example.reduesmobile.databinding.ActivityBusquedaBinding
import kotlinx.coroutines.launch

class BusquedaActivity : AppCompatActivity() {
    lateinit var binding: ActivityBusquedaBinding
    private lateinit var adapter: UsuarioAdapter
    private val usuarios = mutableListOf<PerfilDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusquedaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecycler()
        configurarNavegacion()

        binding.btnBuscar.setOnClickListener { buscar() }
        binding.btnVolver.setOnClickListener { finish() }

        // Buscar al presionar Enter en el teclado
        binding.etBusqueda.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscar()
                true
            } else false
        }
    }

    private fun configurarRecycler() {
        adapter = UsuarioAdapter(usuarios) { usuario ->
            val intent = Intent(this, Perfil::class.java)
            intent.putExtra("usuarioId", usuario.id)
            startActivity(intent)
        }
        binding.recyclerBusqueda.layoutManager = LinearLayoutManager(this)
        binding.recyclerBusqueda.adapter = adapter
    }

    private fun getFiltro(): String {
        return when (binding.radioFiltros.checkedRadioButtonId) {
            R.id.radioActivos -> "activos"
            R.id.radioSiguiendo -> "siguiendo"
            else -> "relevancia"
        }
    }

    private fun buscar() {
        val query = binding.etBusqueda.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        val filtro = getFiltro()
        binding.txtVacio.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@BusquedaActivity)
                    .create(UsuariosApi::class.java)

                val response = api.buscarUsuarios(query, filtro)

                if (response.isSuccessful) {
                    val resultados = response.body() ?: emptyList()
                    adapter.setUsuarios(resultados)

                    if (resultados.isEmpty()) {
                        binding.txtVacio.text = "No se encontraron usuarios"
                        binding.txtVacio.visibility = View.VISIBLE
                    } else {
                        binding.txtVacio.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@BusquedaActivity, "Error al buscar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BusquedaActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
            startActivity(Intent(this, Perfil::class.java))
            finish()
        }
    }
}