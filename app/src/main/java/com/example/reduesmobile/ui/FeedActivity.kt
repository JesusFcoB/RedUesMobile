package com.example.reduesmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reduesmobile.R
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.databinding.ActivityFeedBinding

class FeedActivity : AppCompatActivity() {
    lateinit var binding: ActivityFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.btnLogout.setOnClickListener {
//            TokenManager(this).deleteToken()
//            val logout = Intent(this, MainActivity::class.java)
//            startActivity(logout)
//            finish()
//        }
    }
}