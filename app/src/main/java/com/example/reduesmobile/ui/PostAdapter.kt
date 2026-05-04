package com.example.reduesmobile.ui

import android.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.PublicacionDto

class PostAdapter(
    private val posts: MutableList<PublicacionDto>,
    private val currentUserId: Int,
    private val onLike: (PublicacionDto) -> Unit,
    private val onGuardar: (PublicacionDto) -> Unit,
    private val onComentar: (PublicacionDto) -> Unit,
    private val onEditar: (PublicacionDto) -> Unit,
    private val onEliminar: (PublicacionDto) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUsuario: TextView = view.findViewById(R.id.txtUsuarioPost)
        val txtContenido: TextView = view.findViewById(R.id.txtContenido)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
        val txtLikes: TextView = view.findViewById(R.id.txtLikes)
        val txtGuardados: TextView = view.findViewById(R.id.txtGuardados)
        val txtComentarios: TextView = view.findViewById(R.id.txtComentarios)
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val btnGuardar: ImageView = view.findViewById(R.id.btnGuardar)
        val btnComentar: ImageView = view.findViewById(R.id.btnComentar)
        val btnOpciones: ImageView = view.findViewById(R.id.btnOpciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.txtUsuario.text = post.autor
        holder.txtFecha.text = post.fechaCreacion
        holder.txtLikes.text = post.cantidadLikes.toString()
        holder.txtGuardados.text = post.cantidadGuardados.toString()
        holder.txtComentarios.text = post.comentarios.size.toString()

        holder.txtContenido.text = if (post.tipo == "Apunte") {
            Html.fromHtml(post.contenido, Html.FROM_HTML_MODE_COMPACT)
        } else {
            post.contenido
        }

        holder.btnLike.setImageResource(
            if (post.yaDioLike) R.drawable.like_relleno else R.drawable.like
        )
        holder.btnGuardar.setImageResource(
            if (post.yaGuardo) R.drawable.guardado_relleno else R.drawable.guardado
        )

        holder.btnLike.setOnClickListener { onLike(post) }
        holder.btnGuardar.setOnClickListener { onGuardar(post) }
        holder.btnComentar.setOnClickListener { onComentar(post) }

        // Mostrar opciones solo si es el autor
        if (post.autorId == currentUserId) {
            holder.btnOpciones.visibility = View.VISIBLE
            holder.btnOpciones.setOnClickListener {
                val opciones = arrayOf("Editar", "Eliminar")
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Opciones")
                    .setItems(opciones) { _, which ->
                        when (which) {
                            0 -> onEditar(post)
                            1 -> onEliminar(post)
                        }
                    }
                    .show()
            }
        } else {
            holder.btnOpciones.visibility = View.GONE
        }
    }

    override fun getItemCount() = posts.size

    fun agregarPosts(nuevos: List<PublicacionDto>) {
        val inicio = posts.size
        posts.addAll(nuevos)
        notifyItemRangeInserted(inicio, nuevos.size)
    }

    fun eliminarPost(id: Int) {
        val index = posts.indexOfFirst { it.id == id }
        if (index != -1) {
            posts.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}