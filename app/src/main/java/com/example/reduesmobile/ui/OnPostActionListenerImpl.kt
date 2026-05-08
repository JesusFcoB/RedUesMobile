package com.example.reduesmobile.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.GuardadosApi
import com.example.reduesmobile.data.api.LikesApi
import com.example.reduesmobile.data.dto.PublicacionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OnPostActionListenerImpl(
    private val context: Context,
    private val rvPosts: RecyclerView,
    private val postAdapter: PostAdapter,
    private val scope: CoroutineScope,
) : OnPostActionListener {
    override fun onUserNameClick(post: PublicacionResponse?, position: Int) {
        val perfil = Intent(context, Perfil::class.java)
        perfil.putExtra("idUsuario", post?.idAutor)
        context.startActivity(perfil)
    }

    override fun onLikeClick(post: PublicacionResponse?, position: Int) {
        if (post != null) {
            val holder = rvPosts.findViewHolderForAdapterPosition(position) as? PostAdapter.PostViewHolder
            holder?.btnLike?.isEnabled = false

            post.yaDioLike = !post.yaDioLike
            rvPosts.postDelayed({
                postAdapter.notifyItemChanged(position)
            }, 100)

            scope.launch {
                try {
                    val api = RetrofitInstance.Companion
                        .getRetrofitInstance(context)
                        .create(LikesApi::class.java)
                    val response = api.toggleLike(post.idPublicacion)

                    if (!response.isSuccessful) {
                        post.yaDioLike = !post.yaDioLike
                        postAdapter.notifyItemChanged(position)

                        Toast.makeText(context, "Error al procesar like", Toast.LENGTH_LONG).show()
                    }


                } catch (e: Exception) {
                    post.yaDioLike = !post.yaDioLike
                    postAdapter.notifyItemChanged(position)

                    Toast.makeText(context, "Error al procesar like", Toast.LENGTH_LONG).show()
                } finally {
                    rvPosts.postDelayed({
                        holder?.btnLike?.isEnabled = true
                    }, 500)
                }
            }
        }
    }

    override fun onSaveClick(post: PublicacionResponse?, position: Int) {
        if (post != null) {
            val holder = rvPosts.findViewHolderForAdapterPosition(position) as? PostAdapter.PostViewHolder
            holder?.btnGuardar?.isEnabled = false

            post.yaGuardo = !post.yaGuardo
            rvPosts.postDelayed({
                postAdapter.notifyItemChanged(position)
            }, 100)

            scope.launch {
                try {
                    val api = RetrofitInstance.Companion
                        .getRetrofitInstance(context)
                        .create(GuardadosApi::class.java)
                    val response = api.toggleGuardado(post.idPublicacion)

                    if (!response.isSuccessful) {
                        post.yaGuardo = !post.yaGuardo
                        postAdapter.notifyItemChanged(position)

                        Toast.makeText(context, "Error al procesar guardado", Toast.LENGTH_LONG).show()
                    }


                } catch (e: Exception) {
                    post.yaGuardo = !post.yaGuardo
                    postAdapter.notifyItemChanged(position)

                    Toast.makeText(context, "Error al procesar guardado", Toast.LENGTH_LONG).show()
                } finally {
                    rvPosts.postDelayed({
                        holder?.btnGuardar?.isEnabled = true
                    }, 500)
                }
            }
        }
    }

    override fun onCommentClick(post: PublicacionResponse?, position: Int) {
        TODO("Abrir la publicacion completa")
    }
}