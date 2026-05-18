package com.example.reduesmobile.ui

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.OvershootInterpolator
import androidx.core.text.HtmlCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.PublicacionResponse

class PostAdapter(private var listener: OnPostActionListener?, context: Context):
    PagingDataAdapter<PublicacionResponse, PostAdapter.PostViewHolder>(DiffCallback) {

    private val tokenManager = TokenManager(context)
    private val currentUserId = tokenManager.getUserId()

    fun setListener(newListener: OnPostActionListener) {
        this.listener = newListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        item?.let {
            holder.usuario.text = it.autor

            if(it.tipo == "Apunte") {
                holder.contenido.text = HtmlCompat.fromHtml(it.contenido, HtmlCompat.FROM_HTML_MODE_LEGACY)
                holder.contenido.movementMethod = LinkMovementMethod.getInstance()
            } else {
                holder.contenido.text = it.contenido
            }

            if (it.idAutor == currentUserId) {
                holder.btnEditar.visibility = View.VISIBLE
                holder.btnEliminar.visibility = View.VISIBLE
            } else {
                holder.btnEditar.visibility = View.GONE
                holder.btnEliminar.visibility = View.GONE
            }

            val likeIcon = if (item.yaDioLike) R.drawable.like_relleno else R.drawable.like
            val savedIcon = if (item.yaGuardo) R.drawable.guardado_relleno else R.drawable.guardado
            holder.txtLikesCount.text = it.cantidadLikes.toString()
            holder.txtGuardadosCount.text = it.cantidadGuardados.toString()
            holder.txtComentariosCount.text = it.comentarios.size.toString()

            holder.btnLike.setImageResource(likeIcon)
            holder.btnGuardar.setImageResource(savedIcon)

            holder.btnLike.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    // Obtenemos el ítem exacto que el Adapter tiene en memoria AHORA
                    val latestItem = getItem(currentPos)
                    latestItem?.let {
                        holder.btnLike.bounceAnimation()
                        listener?.onLikeClick(it, currentPos)
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
                        listener?.onSaveClick(it, currentPos)
                    }
                }
            }

            holder.btnCommenar.setOnClickListener { listener?.onCommentClick(item, holder.bindingAdapterPosition) }
            holder.usuario.setOnClickListener { listener?.onUserNameClick(item, holder.bindingAdapterPosition) }

            holder.btnEditar.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val latestItem = getItem(currentPos)
                    latestItem?.let {
                        listener?.onEditClick(it, currentPos)
                    }
                }
            }

            holder.btnEliminar.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val latestItem = getItem(currentPos)
                    latestItem?.let {
                        listener?.onDeleteClick(it, currentPos)
                    }
                }
            }
        }
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Referenciamos los IDs exactos de tu ConstraintLayout
        val usuario: TextView = view.findViewById(R.id.txtUsuarioPost)
        val contenido: TextView = view.findViewById(R.id.txtContenido)
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val btnGuardar: ImageView = view.findViewById(R.id.btnGuardar)
        val btnCommenar: ImageView = view.findViewById(R.id.btnComentar)
        val btnEliminar: ImageView = view.findViewById(R.id.btnEliminar)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditar)
        val txtLikesCount: TextView = view.findViewById(R.id.txtLikeCount)
        val txtGuardadosCount: TextView = view.findViewById(R.id.txtGuardarCount)
        val txtComentariosCount: TextView = view.findViewById(R.id.txtComentarioCount)

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