package com.example.reduesmobile.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.reduesmobile.databinding.ActivityPublicacionBinding

class Publicacion : AppCompatActivity() {

    lateinit var binding: ActivityPublicacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}