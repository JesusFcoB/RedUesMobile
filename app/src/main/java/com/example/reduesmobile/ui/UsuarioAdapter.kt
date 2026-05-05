package com.example.reduesmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reduesmobile.R
import com.example.reduesmobile.data.dto.PerfilDto

class UsuarioAdapter(
    private val usuarios: MutableList<PerfilDto>,
    private val onVerPerfil: (PerfilDto) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombreUsuario)
        val txtCarrera: TextView = view.findViewById(R.id.txtCarreraUsuario)
        val txtSeguidores: TextView = view.findViewById(R.id.txtSeguidores)
        val txtPublicaciones: TextView = view.findViewById(R.id.txtPublicacionesUsuario)
        val btnVer: Button = view.findViewById(R.id.btnVerPerfil)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.txtNombre.text = usuario.userName
        holder.txtCarrera.text = usuario.carrera
        holder.txtSeguidores.text = "${usuario.cantidadSeguidores} seguidores"
        holder.txtPublicaciones.text = "${usuario.cantidadPublicaciones} publicaciones"
        holder.btnVer.setOnClickListener { onVerPerfil(usuario) }
        holder.itemView.setOnClickListener { onVerPerfil(usuario) }
    }

    override fun getItemCount() = usuarios.size

    fun setUsuarios(nuevos: List<PerfilDto>) {
        usuarios.clear()
        usuarios.addAll(nuevos)
        notifyDataSetChanged()
    }
}