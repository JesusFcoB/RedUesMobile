package com.example.reduesmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.ComentarioResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ComentarioAdapter(private val comentarios: MutableList<ComentarioResponse>) :
    RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comentario, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = comentarios[position]
        holder.usuario.text = comentario.usuario
        holder.contenido.text = comentario.texto
        holder.fecha.text = obtenerTiempoRelativo(comentario.fechaComentario)
        
        // Ocultar botones que no se usan en comentarios
        holder.btnLike.visibility = View.GONE
        holder.btnGuardar.visibility = View.GONE
        holder.btnComentar.visibility = View.GONE
    }

    override fun getItemCount(): Int = comentarios.size

    /**
     * Agrega un nuevo comentario a la lista y actualiza la UI al instante
     */
    fun agregarComentario(nuevo: ComentarioResponse) {
        comentarios.add(nuevo)
        notifyItemInserted(comentarios.size - 1)
    }

    private fun obtenerTiempoRelativo(fechaStr: String): String {
        if (fechaStr.isBlank()) return "hace un momento"
        
        val formatters = listOf(
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )

        var past: LocalDateTime? = null
        for (formatter in formatters) {
            try {
                past = if (!fechaStr.contains(":")) {
                    // Si es solo fecha, intentamos parsear con LocalDate y atStartOfDay
                    java.time.LocalDate.parse(fechaStr.split(" ")[0].split("T")[0], formatter).atStartOfDay()
                } else {
                    LocalDateTime.parse(fechaStr, formatter)
                }
                break
            } catch (e: Exception) {
                continue
            }
        }

        if (past == null) return fechaStr.split("T")[0] // Fallback

        val now = LocalDateTime.now()
        val duration = Duration.between(past, now)

        val seconds = duration.seconds
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "hace un momento"
            minutes == 1L -> "hace 1 min"
            minutes < 60 -> "hace $minutes min"
            hours == 1L -> "hace 1 h"
            hours < 24 -> "hace $hours h"
            days == 1L -> "ayer"
            days < 7 -> "hace $days d"
            weeks == 1L -> "hace 1 sem"
            weeks < 4 -> "hace $weeks sem"
            months == 1L -> "hace 1 mes"
            months < 12 -> "hace $months meses"
            years == 1L -> "hace 1 año"
            else -> "hace $years años"
        }
    }

    class ComentarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usuario: TextView = view.findViewById(R.id.txtUsuarioPost)
        val contenido: TextView = view.findViewById(R.id.txtContenido)
        val fecha: TextView = view.findViewById(R.id.txtFechaComentario)
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val btnGuardar: ImageView = view.findViewById(R.id.btnGuardar)
        val btnComentar: ImageView = view.findViewById(R.id.btnComentar)
    }
}
