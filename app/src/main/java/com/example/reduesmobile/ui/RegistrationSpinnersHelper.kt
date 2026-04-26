package com.example.reduesmobile.ui

import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.reduesmobile.R
import com.example.reduesmobile.data.Carreras

class RegistrationSpinnersHelper(private val activity: android.content.Context) {

    fun llenarCarreras(spCarrera: Spinner) {
        val nombreCarreras = Carreras.listaCarreras
            .map { carrera -> "${carrera.nombre} (${carrera.nombreCorto})" }
            .toMutableList()

        nombreCarreras.add(0,"Selecciona tu carrera")

        val carrerasAdapter = ArrayAdapter(
            activity,
            R.layout.spinner_item,
            nombreCarreras
        )

        carrerasAdapter.setDropDownViewResource(R.layout.spinner_item)
        spCarrera.adapter = carrerasAdapter
    }

    fun llenarSemestres(nombreCarrera: String, spSemestre: Spinner) {
        val nSemestres = Carreras.listaCarreras
            .find { c -> c.nombre == nombreCarrera }
            ?.semestres

        val nSemestresAdapter: MutableList<String> = mutableListOf()
        if (nSemestres != null) {
            for (i in 1..nSemestres) {
                nSemestresAdapter.add(i.toString())
            }
        }
        nSemestresAdapter.add(0, "Selecciona semestre")

        val semestresAdapter = ArrayAdapter(
            activity,
            R.layout.spinner_item,
            nSemestresAdapter
        )

        semestresAdapter.setDropDownViewResource(R.layout.spinner_item)
        spSemestre.adapter = semestresAdapter
    }
}