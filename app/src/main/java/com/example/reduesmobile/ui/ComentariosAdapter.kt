package com.example.reduesmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.ComentarioDto

class ComentariosAdapter(
    private val comentarios: MutableList<ComentarioDto>
) : RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder>() {

    inner class ComentarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUsuario: TextView = view.findViewById(R.id.txtUsuarioComentario)
        val txtTexto: TextView = view.findViewById(R.id.txtTextoComentario)
        val txtFecha: TextView = view.findViewById(R.id.txtFechaComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = comentarios[position]
        holder.txtUsuario.text = comentario.usuario
        holder.txtTexto.text = comentario.texto
        holder.txtFecha.text = comentario.fecha_comentario
    }

    override fun getItemCount() = comentarios.size

    fun agregar(comentario: ComentarioDto) {
        comentarios.add(0, comentario)
        notifyItemInserted(0)
    }
}