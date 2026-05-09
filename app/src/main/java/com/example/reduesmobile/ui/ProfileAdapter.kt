package com.example.reduesmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.PerfilResponse

class ProfileAdapter(
    private val onItemClick: (PerfilResponse) -> Unit
)  : ListAdapter<PerfilResponse, ProfileAdapter.PostViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position)

        holder.usuario.text = item.userName
        holder.verPerfil.setOnClickListener {
            onItemClick(item)
        }
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usuario: TextView = view.findViewById(R.id.txtUserName)
        val verPerfil: Button = view.findViewById(R.id.btnVerPerfil)
    }

    object DiffCallback : DiffUtil.ItemCallback<PerfilResponse>() {
        override fun areItemsTheSame(
            oldItem: PerfilResponse,
            newItem: PerfilResponse
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PerfilResponse,
            newItem: PerfilResponse
        ) = oldItem == newItem
    }
}