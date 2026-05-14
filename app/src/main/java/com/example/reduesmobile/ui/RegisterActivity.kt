package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.AuthApi
import com.example.reduesmobile.data.auth.TokenManager
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
        spinnerHelper.llenarSemestres("x", binding.spSemestre)
        listener()

        binding.btnRegister.setOnClickListener {
            // 1. Validar campos y mostrar error si existe
            val mensajeError = validarCampos()
            if (mensajeError != null) {
                Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 2. Capturar y limpiar datos (Trimming)
            val usuario = binding.txtUsuario.text.toString().trim()
            val correo = binding.txtEmail.text.toString().trim()
            val carrera = binding.spCarrera.selectedItem.toString().substringBefore(" (")
            val semestreStr = binding.spSemestre.selectedItem.toString()
            val semestre = semestreStr.toInt()
            val password = binding.txtPassword.text.toString()
            val confirmPassword = binding.txtConfirmPassword.text.toString()

            val registerRequest = RegisterRequest(
                usuario, correo, carrera, semestre, password, confirmPassword
            )

            registrar(registerRequest)
        }
    }

    private fun registrar(request: RegisterRequest) {
        binding.loadingCardRegister.visibility = View.VISIBLE
        binding.cargandoRegister.visibility = View.VISIBLE
        bloquearUIRegister()

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@RegisterActivity)
                    .create(AuthApi::class.java)

                val response = api.register(request)

                if (response.isSuccessful) {
                    val loginRequest = LoginRequest(request.nombreUsuario, request.password)
                    iniciarSesion(api, loginRequest)
                } else {
                    // Mejora: Intentar extraer el mensaje de error del servidor si existe
                    val errorMsg = response.errorBody()?.string() ?: "Datos incorrectos o usuario ya existe"
                    Toast.makeText(this@RegisterActivity, "Fallo en el registro: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error de red: Comprueba tu conexión", Toast.LENGTH_LONG).show()
            } finally {
                desbloquearUIRegister()
                binding.loadingCardRegister.visibility = View.GONE
                binding.cargandoRegister.visibility = View.GONE
            }
        }
    }

    private fun validarCampos(): String? {
        val usuario = binding.txtUsuario.text.toString().trim()
        val correo = binding.txtEmail.text.toString().trim()
        val password = binding.txtPassword.text.toString()
        val confirmPassword = binding.txtConfirmPassword.text.toString()

        // Validaciones de Usuario
        if (usuario.isEmpty()) return "El nombre de usuario es obligatorio"
        if (usuario.length < 3) return "El usuario debe tener al menos 3 caracteres"
        if (usuario.contains(" ")) return "El nombre de usuario no puede contener espacios"

        // Validaciones de Correo
        if (correo.isEmpty()) return "El correo electrónico es obligatorio"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) return "Ingresa un correo electrónico válido"

        // Validaciones de Spinners
        if (binding.spCarrera.selectedItemPosition <= 0) return "Por favor, selecciona tu carrera"
        if (binding.spSemestre.selectedItemPosition <= 0) return "Por favor, selecciona tu semestre"

        // Validaciones de Contraseña
        if (password.isEmpty()) return "La contraseña es obligatoria"
        if (password.length < 8) return "La contraseña debe tener al menos 8 caracteres"
        
        // Validación de complejidad (Mayúscula, minúscula y número)
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$".toRegex()
        if (!password.matches(passwordRegex)) {
            return "La contraseña debe incluir al menos una mayúscula, una minúscula y un número"
        }

        if (password != confirmPassword) return "Las contraseñas no coinciden"

        return null // Todo correcto
    }

    private fun listener() {
        binding.spCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val carreraSeleccionada = parent?.getItemAtPosition(position).toString()
                    val nombreReal = carreraSeleccionada.substringBefore(" (")
                    spinnerHelper.llenarSemestres(nombreReal, binding.spSemestre)
                } else {
                    spinnerHelper.llenarSemestres("x", binding.spSemestre)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private suspend fun iniciarSesion(api: AuthApi, loginRequest: LoginRequest) {
        try {
            val login = api.login(loginRequest)
            if (login.isSuccessful) {
                login.body()?.let { authData ->
                    TokenManager(this@RegisterActivity).saveToken(authData.token, authData.expiracion)
                    startActivity(Intent(this@RegisterActivity, FeedActivity::class.java))
                    finish()
                }
            } else {
                Toast.makeText(this, "Registro exitoso, pero error al iniciar sesión automática", Toast.LENGTH_SHORT).show()
                finish() // Cerramos registro para que el usuario haga login manual
            }
        } catch (e: Exception) {
            finish()
        }
    }

    private fun bloquearUIRegister() {
        binding.txtUsuario.isEnabled = false
        binding.txtEmail.isEnabled = false
        binding.txtPassword.isEnabled = false
        binding.txtConfirmPassword.isEnabled = false
        binding.spCarrera.isEnabled = false
        binding.spSemestre.isEnabled = false
        binding.btnRegister.isEnabled = false
    }

    private fun desbloquearUIRegister() {
        binding.txtUsuario.isEnabled = true
        binding.txtEmail.isEnabled = true
        binding.txtPassword.isEnabled = true
        binding.txtConfirmPassword.isEnabled = true
        binding.spCarrera.isEnabled = true
        binding.spSemestre.isEnabled = true
        binding.btnRegister.isEnabled = true
    }
}
