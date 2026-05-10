package com.example.reduesmobile.ui

import android.os.Bundle
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
    }
}