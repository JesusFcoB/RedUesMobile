package com.example.reduesmobile.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reduesmobile.data.Carreras
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.UsuariosApi
import com.example.reduesmobile.data.dto.EditarPerfilRequest
import com.example.reduesmobile.databinding.ActivityEditarPerfilBinding
import kotlinx.coroutines.launch

class EditarPerfilActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditarPerfilBinding
    private var semestreSeleccionado = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos actuales del perfil
        val bioActual = intent.getStringExtra("bio") ?: ""
        val carreraActual = intent.getStringExtra("carrera") ?: ""
        val semestreActual = intent.getIntExtra("semestre", 1)

        binding.etBio.setText(bioActual)

        configurarSpinnerCarrera(carreraActual)
        configurarSpinnerSemestre(semestreActual)

        binding.btnVolver.setOnClickListener { finish() }

        binding.btnGuardar.setOnClickListener {
            guardarPerfil()
        }
    }

    private fun configurarSpinnerCarrera(carreraActual: String) {
        val carreras = listOf("Selecciona una carrera") +
                Carreras.listaCarreras.map { "${it.nombre} (${it.nombreCorto})" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, carreras)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCarrera.adapter = adapter

        // Seleccionar la carrera actual
        val index = carreras.indexOfFirst { it.contains(carreraActual) }
        if (index != -1) binding.spCarrera.setSelection(index)

        binding.spCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val carreraSeleccionada = Carreras.listaCarreras[position - 1]
                    configurarSpinnerSemestre(semestreSeleccionado, carreraSeleccionada.semestres)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarSpinnerSemestre(semestreActual: Int, maxSemestres: Int = 8) {
        val semestres = listOf("Selecciona semestre") +
                (1..maxSemestres).map { it.toString() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semestres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSemestre.adapter = adapter

        val index = if (semestreActual in 1..maxSemestres) semestreActual else 0
        binding.spSemestre.setSelection(index)

        binding.spSemestre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                semestreSeleccionado = if (position > 0) position else 1
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun guardarPerfil() {
        val bio = binding.etBio.text.toString().trim()
        val carreraPos = binding.spCarrera.selectedItemPosition
        val semestrePos = binding.spSemestre.selectedItemPosition

        if (carreraPos == 0) {
            Toast.makeText(this, "Selecciona una carrera", Toast.LENGTH_SHORT).show()
            return
        }
        if (semestrePos == 0) {
            Toast.makeText(this, "Selecciona un semestre", Toast.LENGTH_SHORT).show()
            return
        }

        val carrera = Carreras.listaCarreras[carreraPos - 1].nombre
        val semestre = semestrePos

        binding.btnGuardar.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(this@EditarPerfilActivity)
                    .create(UsuariosApi::class.java)

                val response = api.editarPerfil(
                    EditarPerfilRequest(bio = bio, carrera = carrera, semestre = semestre)
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@EditarPerfilActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditarPerfilActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditarPerfilActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnGuardar.isEnabled = true
            }
        }
    }
}