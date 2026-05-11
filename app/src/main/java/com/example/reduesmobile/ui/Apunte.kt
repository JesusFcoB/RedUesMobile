package com.example.reduesmobile.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.reduesmobile.R
import com.example.reduesmobile.databinding.ActivityApunteBinding
import com.example.reduesmobile.databinding.ActivityPublicacionBinding

class Apunte : AppCompatActivity() {
    lateinit var binding: ActivityApunteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApunteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEditor()

        NavigationHelper.setupBottomNavigation(this, binding.bottomNavigation)
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

    private fun obtenerTextoDelEditor() {
        // Ejecutamos la función JavaScript definida en el HTML
        binding.webViewEditor.evaluateJavascript("editorInstance.getData();") { htmlContent ->
            // El htmlContent viene entre comillas (ej: "<p>Hola</p>")
            val cleanHtml = htmlContent.removeSurrounding("\"").replace("\\u003C", "<")
            Log.d("EDITOR", "Contenido HTML: $cleanHtml")
        }
    }
}