package com.example.reduesmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.PublicacionResponse

class PostAdapter: PagingDataAdapter<PublicacionResponse, PostAdapter.PostViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.usuario.text = it.autor
            holder.contenido.text = it.contenido
        }
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Referenciamos los IDs exactos de tu ConstraintLayout
        val usuario: TextView = view.findViewById(R.id.txtUsuarioPost)
        val contenido: TextView = view.findViewById(R.id.txtContenido)
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val btnGuardar: ImageView = view.findViewById(R.id.btnGuardar)
    }

    object DiffCallback : DiffUtil.ItemCallback<PublicacionResponse>() {
        override fun areItemsTheSame(old: PublicacionResponse, new: PublicacionResponse) = old.idPublicacion == new.idPublicacion
        override fun areContentsTheSame(old: PublicacionResponse, new: PublicacionResponse) = old == new
    }
}