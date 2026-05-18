package com.example.reduesmobile.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.R
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.dto.PublicacionRequest
import com.example.reduesmobile.databinding.ActivityApunteBinding
import com.example.reduesmobile.databinding.ActivityPublicacionBinding
import kotlinx.coroutines.launch

class Apunte : AppCompatActivity() {
    lateinit var binding: ActivityApunteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApunteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEditor()

        NavigationHelper.setupBottomNavigation(this, binding.bottomNavigation)

        binding.btnAgregarApunte.setOnClickListener {
            binding.webViewEditor.evaluateJavascript("editorInstance.getData();") { htmlContent ->
                val contenido = htmlContent.removeSurrounding("\"")
                    .replace("\\u003C", "<")
                    .replace("\\u003E", ">")
                    .replace("\\\"", "\"")
                    .trim()

                if (contenido.isNotBlank() && contenido != "<p></p>") {
                    val request = PublicacionRequest(contenido, "Apunte")
                    publicar(request)

                } else {
                    Toast.makeText(
                        this,
                        "Por favor, escribe algo antes de publicar",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
        binding.btnApuntesVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupEditor() {
        binding.webViewEditor.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true // Necesario para CKEditor

            // Cargamos el archivo local de assets
            loadUrl("file:///android_asset/editor.html")
        }
    }

    private fun limpiarFormulario() {
        binding.webViewEditor.evaluateJavascript("clearEditorData();", null)
        binding.webViewEditor.clearFocus()
    }

    private fun publicar(request: PublicacionRequest) {
        lifecycleScope.launch {
            try {
                binding.btnAgregarApunte.isEnabled = false

                val api = RetrofitInstance.Companion
                    .getRetrofitInstance(this@Apunte)
                    .create(PublicacionesApi::class.java)

                val response = api.crearPublicacion(request)

                if (response.isSuccessful) {
                    limpiarFormulario()
                    Toast.makeText(
                        this@Apunte,
                        "Apunte agregado exitosamente",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val errorBody = response.errorBody()?.string()

                    Toast.makeText(
                        this@Apunte,
                        "Error al publicar: ${errorBody ?: "Datos incorrectos"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Apunte, "Error de red: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            } finally {
                binding.btnAgregarApunte.isEnabled = true
            }
        }
    }
}