package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.Carreras
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.AuthApi
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.CarreraDto
import com.example.reduesmobile.data.dto.LoginRequest
import com.example.reduesmobile.data.dto.RegisterRequest
import com.example.reduesmobile.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    var spinnerHelper = RegistrationSpinnersHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spinnerHelper.llenarCarreras(binding.spCarrera)
        spinnerHelper.llenarSemestres("x",binding.spSemestre)
        listener()

        binding.btnRegister.setOnClickListener {
            val usuario = binding.txtUsuario.text.toString()
            val correo = binding.txtEmail.text.toString()
            val carrera = binding.spCarrera.selectedItem.toString().substringBefore(" (")
            val semestre = binding.spSemestre.selectedItem.toString().toInt()
            val password = binding.txtPassword.text.toString()
            val confirmPassword = binding.txtConfirmPassword.text.toString()


            //TODO: validar las entradas antes de crear el objeto
            val registerRequest: RegisterRequest = RegisterRequest(
                usuario, correo, carrera, semestre, password, confirmPassword
            )
            registrar(registerRequest)
        }

    }


    private fun registrar(request: RegisterRequest) {
        binding.loadingCardRegister.visibility = View.VISIBLE
        binding.cargandoRegister.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.Companion
                    .getRetrofitInstance(this@RegisterActivity)
                    .create(AuthApi::class.java)

                val response = api.register(request)

                if (response.isSuccessful) {

                    val loginRequest: LoginRequest = LoginRequest(
                        request.nombreUsuario,
                        request.password)
                    iniciarSesion(api, loginRequest)
                } else {
                    Toast.makeText(this@RegisterActivity, "Error de credenciales", Toast.LENGTH_LONG)
                        .show()
                    //TODO: personalizar error de inicio de sesion fallido
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            } finally {
                binding.loadingCardRegister.visibility = View.GONE
                binding.cargandoRegister.visibility = View.GONE
            }
        }
    }
    private fun listener() {
        binding.spCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if(position > 0) {
                    val carreraSeleccionada =  parent?.getItemAtPosition(position).toString()
                    val nombreReal = carreraSeleccionada.substringBefore(" (")
                    spinnerHelper.llenarSemestres(nombreReal, binding.spSemestre)
                } else {
                    spinnerHelper.llenarSemestres("x", binding.spSemestre)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    private suspend fun iniciarSesion(api: AuthApi, loginRequest: LoginRequest) {
        val login = api.login(loginRequest)

        if (login.isSuccessful) {
            val authData = login.body()

            authData?.let { token ->
                TokenManager(this@RegisterActivity).saveToken(
                    authData.token,
                    authData.expiracion
                )
                val login = Intent(this@RegisterActivity, FeedActivity::class.java)
                startActivity(login)
                finish()
            }
        }
    }
}