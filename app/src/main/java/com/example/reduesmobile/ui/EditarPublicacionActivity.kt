package com.example.reduesmobile.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.R
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.data.dto.PublicacionRequest
import com.example.reduesmobile.data.dto.PublicacionResponse
import android.webkit.WebView
import kotlinx.coroutines.launch

class EditarPublicacion : AppCompatActivity() {

    private lateinit var webViewEditor: WebView
    private lateinit var etContenido: EditText
    private lateinit var btnGuardar: Button

    private var idPublicacion: Int = -1
    private var tipo: String = ""
    private var contenidoActual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_publicacion)

        webViewEditor = findViewById(R.id.webViewEditorEditar)
        etContenido = findViewById(R.id.etContenidoEditar)
        btnGuardar = findViewById(R.id.btnGuardarEdicion)

        // Recibimos los datos del post
        idPublicacion = intent.getIntExtra("idPublicacion", -1)
        tipo = intent.getStringExtra("tipo") ?: ""
        contenidoActual = intent.getStringExtra("contenido") ?: ""

        if (tipo == "Apunte") {
            webViewEditor.visibility = View.VISIBLE
            etContenido.visibility = View.GONE
            setupWebViewEditor()
        } else {
            webViewEditor.visibility = View.GONE
            etContenido.visibility = View.VISIBLE
            etContenido.setText(contenidoActual)
        }

        btnGuardar.setOnClickListener {
            if (tipo == "Apunte") {
                guardarApunte()
            } else {
                guardarPublicacion()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewEditor() {
        webViewEditor.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            loadUrl("file:///android_asset/editor.html")

            // Esperamos a que cargue el editor para poner el contenido actual
            webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Escapamos el contenido para inyectarlo de forma segura
                    val escaped = contenidoActual
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "")
                    view?.evaluateJavascript(
                        "editorInstance.setData(\"$escaped\");", null
                    )
                }
            }
        }
    }

    private fun guardarApunte() {
        webViewEditor.evaluateJavascript("editorInstance.getData();") { htmlContent ->
            val contenido = htmlContent.removeSurrounding("\"")
                .replace("\\u003C", "<")
                .replace("\\u003E", ">")
                .replace("\\\"", "\"")
                .trim()

            if (contenido.isNotBlank() && contenido != "<p></p>") {
                enviarEdicion(contenido)
            } else {
                Toast.makeText(this, "El contenido no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarPublicacion() {
        val contenido = etContenido.text.toString().trim()
        if (contenido.isNotBlank()) {
            enviarEdicion(contenido)
        } else {
            Toast.makeText(this, "El contenido no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarEdicion(contenido: String) {
        lifecycleScope.launch {
            try {
                btnGuardar.isEnabled = false
                val api = RetrofitInstance.getRetrofitInstance(this@EditarPublicacion)
                    .create(PublicacionesApi::class.java)

                val response = api.editarPublicacion(idPublicacion, PublicacionRequest(contenido, tipo))

                if (response.isSuccessful) {
                    Toast.makeText(this@EditarPublicacion, "Publicación actualizada", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditarPublicacion, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditarPublicacion, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnGuardar.isEnabled = true
            }
        }
    }
}