package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.Carreras
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.AuthApi
import com.example.reduesmobile.data.api.UsuariosApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.PerfilResponse
import com.example.reduesmobile.databinding.ActivityFeedBinding
import com.example.reduesmobile.databinding.ActivityPerfilBinding
import kotlinx.coroutines.launch

class Perfil : AppCompatActivity() {
    lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idUsuario = intent.getIntExtra("idUsuario", 0)
        llenarPerfil(idUsuario)

    }

    private fun llenarPerfil(idUsuario: Int) {
        lifecycleScope.launch {

            val perfil = obtenerPerfil(idUsuario)
            if (perfil != null) {
                binding.txtUsuarioPerfil.text = perfil.userName
                binding.txtCarrera.text = Carreras.listaCarreras.find { c -> c.nombre==perfil.carrera}?.nombreCorto
                binding.txtBio.text = perfil.bio
                binding.txtSemestre.text = " - ${perfil.semestre} semestre"
                binding.contSeguidos.text = perfil.cantidadSiguiendo.toString()
                binding.contSeguidores.text = perfil.cantidadSeguidores.toString()
                binding.contPublicaciones.text = perfil.cantidadPublicaciones.toString()

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

}