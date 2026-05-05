package com.example.reduesmobile

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.PublicacionesApi
import com.example.reduesmobile.databinding.ActivityApunteBinding
import com.example.reduesmobile.ui.FeedActivity
import com.example.reduesmobile.ui.Publicacion
import jp.wasabeef.richeditor.RichEditor
import kotlinx.coroutines.launch

class Apunte : AppCompatActivity() {
    lateinit var binding: ActivityApunteBinding
    private lateinit var editor: RichEditor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApunteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEditor()
        configurarFormato()
        configurarNavegacion()

        binding.btnAgregarApunte.setOnClickListener {
            val contenido = editor.html
            if (contenido.isNullOrEmpty() || contenido == "<br>") {
                Toast.makeText(this, "Escribe algo antes de publicar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            publicarApunte(contenido)
        }
    }

    private fun configurarEditor() {
        editor = binding.editor
        editor.setEditorHeight(200)
        editor.setEditorFontSize(16)
        editor.setEditorFontColor(Color.parseColor("#000000"))
        editor.setPadding(8, 8, 8, 8)
        editor.setPlaceholder("Escribe tu apunte aquí...")
    }

    private fun configurarFormato() {
        binding.btnNegrita.setOnClickListener { editor.setBold() }
        binding.btnCursiva.setOnClickListener { editor.setItalic() }
        binding.btnSubrayado.setOnClickListener { editor.setUnderline() }
        binding.btnTitulo.setOnClickListener { editor.setHeading(1) }
        binding.btnLista.setOnClickListener { editor.setBullets() }

        // Acción para quitar todo el formato del texto seleccionado
        binding.btnLimpiar.setOnClickListener {
            editor.removeFormat() // Esto quita negritas, cursivas, etc., del texto seleccionado
        }

        binding.btnDeshacer.setOnClickListener { editor.undo() }
    }

    private fun publicarApunte(contenido: String) {
        binding.btnAgregarApunte.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@Apunte)
                    .create(PublicacionesApi::class.java)

                val response = api.crearApunte(
                    CrearPublicacionRequest(contenido = contenido, tipo = "Apunte")
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@Apunte, "Apunte publicado", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Apunte, FeedActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Apunte, "Error al publicar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Apunte, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnAgregarApunte.isEnabled = true
            }
        }
    }

    private fun configurarNavegacion() {
        binding.btnInicio.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        binding.btnCrearPublicacion.setOnClickListener {
            startActivity(Intent(this, Publicacion::class.java))
            finish()
        }
        binding.btnCrearApunte.setOnClickListener { }
        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
            finish()
        }
    }
}