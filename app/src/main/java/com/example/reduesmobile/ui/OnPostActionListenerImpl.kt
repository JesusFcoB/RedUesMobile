package com.example.reduesmobile.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            if (post.yaDioLike) {
                post.cantidadLikes += 1
            } else {
                post.cantidadLikes -= 1
            }
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
                        revertirLike(post, position)
                        postAdapter.notifyItemChanged(position)

                        Toast.makeText(context, "Error al procesar like", Toast.LENGTH_LONG).show()
                    }


                } catch (e: Exception) {
                    revertirLike(post, position)
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
            if (post.yaGuardo) {
                post.cantidadGuardados += 1
            } else {
                post.cantidadGuardados -= 1
            }
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
                        revertirGuardado(post, position)
                        postAdapter.notifyItemChanged(position)

                        Toast.makeText(context, "Error al procesar guardado", Toast.LENGTH_LONG).show()
                    }


                } catch (e: Exception) {
                    revertirGuardado(post, position)
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
        if (post != null && context is AppCompatActivity) {
            // USAR newInstance PARA EVITAR ERRORES DE INSTANCIACIÓN
            val bottomSheet = CommentsBottomSheetFragment.newInstance(post)
            bottomSheet.show(context.supportFragmentManager, CommentsBottomSheetFragment.TAG)
        }
    }

    override fun onEditClick(
        post: PublicacionResponse?,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onDeleteClick(
        post: PublicacionResponse?,
        position: Int
    ) {
        TODO("Not yet implemented")
    }


    private fun revertirLike(post: PublicacionResponse, position: Int) {
        post.yaDioLike = !post.yaDioLike
        if (post.yaDioLike) post.cantidadLikes += 1 else post.cantidadLikes -= 1
        postAdapter.notifyItemChanged(position)
    }

    private fun revertirGuardado(post: PublicacionResponse, position: Int) {
        post.yaGuardo = !post.yaGuardo
        if (post.yaGuardo) post.cantidadGuardados += 1 else post.cantidadGuardados -= 1
        postAdapter.notifyItemChanged(position)
    }
}
