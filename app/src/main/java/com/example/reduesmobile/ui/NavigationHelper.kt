package com.example.reduesmobile.ui

import android.app.Activity
import android.content.Intent
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.databinding.ToolbarNavigationBinding

object NavigationHelper {
    fun setupBottomNavigation(activity: Activity, navBinding: ToolbarNavigationBinding, onReselect: (() -> Unit)? = null) {

        navBinding.btnInicio.setOnClickListener {
            if (activity is FeedActivity) {
                onReselect?.invoke()
            } else {
                val intent = Intent(activity, FeedActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                activity.startActivity(intent)
            }
        }

        navBinding.btnPerfil.setOnClickListener {
            if (activity is Perfil) {
                onReselect?.invoke()
            } else {
                val idUsuario = TokenManager(activity).getUserId()
                val intent = Intent(activity, Perfil::class.java).apply {
                    putExtra("idUsuario", idUsuario)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                activity.startActivity(intent)
            }
        }

        navBinding.btnCrearPublicacion.setOnClickListener {
            if (activity !is Publicacion) {
                val intent = Intent(activity, Publicacion::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                activity.startActivity(intent)
            }
        }


        navBinding.btnCrearApunte.setOnClickListener {
            if (activity !is Apunte) {
                val intent = Intent(activity, Apunte::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                activity.startActivity(intent)
            }
        }

    }
}