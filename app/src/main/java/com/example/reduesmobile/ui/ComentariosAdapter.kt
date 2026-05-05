package com.example.reduesmobile.ui

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.ComentarioDto

class ComentariosAdapter(
    private val comentarios: MutableList<ComentarioDto>,
    private val currentUserId: Int,
    private val onEditar: (ComentarioDto) -> Unit,
    private val onEliminar: (ComentarioDto) -> Unit
) : RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder>() {

    inner class ComentarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUsuario: TextView = view.findViewById(R.id.txtUsuarioComentario)
        val txtTexto: TextView = view.findViewById(R.id.txtTextoComentario)
        val txtFecha: TextView = view.findViewById(R.id.txtFechaComentario)
        val btnOpciones: ImageView = view.findViewById(R.id.btnOpcionesComentario)
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

        // Mostrar opciones solo si es el autor
        if (comentario.usuario_id == currentUserId) {
            holder.btnOpciones.visibility = View.VISIBLE
            holder.btnOpciones.setOnClickListener {
                val opciones = arrayOf("Editar", "Eliminar")
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Opciones")
                    .setItems(opciones) { _, which ->
                        when (which) {
                            0 -> onEditar(comentario)
                            1 -> onEliminar(comentario)
                        }
                    }
                    .show()
            }
        } else {
            holder.btnOpciones.visibility = View.GONE
        }
    }

    override fun getItemCount() = comentarios.size

    fun agregar(comentario: ComentarioDto) {
        comentarios.add(0, comentario)
        notifyItemInserted(0)
    }

    fun actualizar(comentario: ComentarioDto) {
        val index = comentarios.indexOfFirst { it.comentario_id == comentario.comentario_id }
        if (index != -1) {
            comentarios[index] = comentario
            notifyItemChanged(index)
        }
    }

    fun eliminar(id: Int) {
        val index = comentarios.indexOfFirst { it.comentario_id == id }
        if (index != -1) {
            comentarios.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}