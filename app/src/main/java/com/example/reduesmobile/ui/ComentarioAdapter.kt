package com.example.reduesmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.ComentarioResponse

class ComentarioAdapter(private val comentarios: List<ComentarioResponse>) :
    RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comentario, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = comentarios[position]
        holder.usuario.text = comentario.usuario
        holder.contenido.text = comentario.texto
        
        // Como es un duplicado de item_post, ocultamos los botones que no aplican a comentarios
        holder.btnLike.visibility = View.GONE
        holder.btnGuardar.visibility = View.GONE
        holder.btnComentar.visibility = View.GONE
    }

    override fun getItemCount(): Int = comentarios.size

    class ComentarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usuario: TextView = view.findViewById(R.id.txtUsuarioPost)
        val contenido: TextView = view.findViewById(R.id.txtContenido)
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val btnGuardar: ImageView = view.findViewById(R.id.btnGuardar)
        val btnComentar: ImageView = view.findViewById(R.id.btnComentar)
    }
}
