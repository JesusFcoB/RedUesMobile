package com.example.reduesmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.OvershootInterpolator
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.PublicacionResponse

class PostAdapter(private val listener: OnPostActionListener):
    PagingDataAdapter<PublicacionResponse, PostAdapter.PostViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.usuario.text = it.autor
            holder.contenido.text = it.contenido

            val likeIcon = if (item.yaDioLike) R.drawable.like_relleno else R.drawable.like
            val savedIcon = if (item.yaGuardo) R.drawable.guardado_relleno else R.drawable.guardado

            holder.btnLike.setImageResource(likeIcon)
            holder.btnGuardar.setImageResource(savedIcon)

            holder.btnLike.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    // Obtenemos el ítem exacto que el Adapter tiene en memoria AHORA
                    val latestItem = getItem(currentPos)
                    latestItem?.let {
                        holder.btnLike.bounceAnimation()
                        listener.onLikeClick(it, currentPos)
                    }
                }
            }

            holder.btnGuardar.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    // Obtenemos el ítem exacto que el Adapter tiene en memoria AHORA
                    val latestItem = getItem(currentPos)
                    latestItem?.let {
                        holder.btnGuardar.bounceAnimation()
                        listener.onSaveClick(it, currentPos)
                    }
                }
            }

            holder.btnCommenar.setOnClickListener { listener.onCommentClick(item, holder.bindingAdapterPosition) }
            holder.usuario.setOnClickListener { listener.onUserNameClick(item, holder.bindingAdapterPosition) }
        }


    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Referenciamos los IDs exactos de tu ConstraintLayout
        val usuario: TextView = view.findViewById(R.id.txtUsuarioPost)
        val contenido: TextView = view.findViewById(R.id.txtContenido)
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val btnGuardar: ImageView = view.findViewById(R.id.btnGuardar)
        val btnCommenar: ImageView = view.findViewById(R.id.btnComentar)

    }

    object DiffCallback : DiffUtil.ItemCallback<PublicacionResponse>() {
        override fun areItemsTheSame(old: PublicacionResponse, new: PublicacionResponse) =
            old.idPublicacion == new.idPublicacion

        override fun areContentsTheSame(
            old: PublicacionResponse,
            new: PublicacionResponse
        ): Boolean {
            // Comparamos los estados que cambian visualmente
            return old.yaDioLike == new.yaDioLike && old.yaGuardo == new.yaGuardo
        }
    }
}

fun View.bounceAnimation() {
    this.scaleX = 0.7f
    this.scaleY = 0.7f
    this.animate()
        .scaleX(1.0f)
        .scaleY(1.0f)
        .setDuration(200)
        .setInterpolator(OvershootInterpolator())
        .start()
}