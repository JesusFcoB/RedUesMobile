package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.AuthApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.LoginRequest
import com.example.reduesmobile.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (TokenManager(this).isValid()) {
            val login = Intent(this, FeedActivity::class.java)
            startActivity(login)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val loginRequest: LoginRequest = LoginRequest(
                binding.txtUsuario.text.toString(),
                binding.txtPassword.text.toString()
            )
            iniciarSesion(loginRequest)
        }

        binding.txtRegister.setOnClickListener {
            val register = Intent(this, RegisterActivity::class.java)
            startActivity(register)
        }

    }

    private fun iniciarSesion(loginRequest: LoginRequest) {
        binding.loadingCard.visibility = View.VISIBLE
        binding.cargando.visibility = View.VISIBLE
        deshabilitarClicks()

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.Companion
                                            .getRetrofitInstance(this@MainActivity)
                                            .create(AuthApi::class.java)

                val response = api.login(loginRequest)

                if (response.isSuccessful) {
                    val authData = response.body()

                    authData?.let { token ->
                        TokenManager(this@MainActivity).saveToken(authData.token, authData.expiracion)

                        val login = Intent(this@MainActivity, FeedActivity::class.java)
                        startActivity(login)
                        finish()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error de credenciales", Toast.LENGTH_LONG).show()
                    // Personalizar error de inicio de sesion fallido
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.loadingCard.visibility = View.GONE
                binding.cargando.visibility = View.GONE
                habilitarClicks()
            }
        }
    }

    private fun deshabilitarClicks() {
        binding.txtUsuario.isFocusable = false
        binding.txtUsuario.isFocusableInTouchMode = false
        binding.txtPassword.isFocusable = false
        binding.txtPassword.isFocusableInTouchMode = false
        binding.btnLogin.isEnabled = false
        binding.txtRegister.isEnabled = false
    }

    private fun habilitarClicks() {
        binding.txtUsuario.isFocusable = true
        binding.txtUsuario.isFocusableInTouchMode = true
        binding.txtPassword.isFocusable = true
        binding.txtPassword.isFocusableInTouchMode = true
        binding.btnLogin.isEnabled = true
        binding.txtRegister.isEnabled = true
    }

}