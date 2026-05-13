package com.example.reduesmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reduesmobile.data.RetrofitInstance
import com.example.reduesmobile.data.api.ComentariosApi
import com.example.reduesmobile.data.dto.ComentarioRequest
import com.example.reduesmobile.data.dto.PublicacionResponse
import com.example.reduesmobile.databinding.BottomSheetComentariosBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetComentariosBinding? = null
    private val binding get() = _binding!!
    private var post: PublicacionResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        post = arguments?.getSerializable(ARG_POST) as? PublicacionResponse
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetComentariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        post?.let {
            val adapter = ComentarioAdapter(it.comentarios)
            binding.rvComentarios.layoutManager = LinearLayoutManager(requireContext())
            binding.rvComentarios.adapter = adapter
        }
    }

    private fun setupSendButton() {
        binding.btnEnviarComentario.setOnClickListener {
            val texto = binding.etNuevoComentario.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarComentario(texto)
            } else {
                Toast.makeText(requireContext(), "Escribe un comentario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarComentario(texto: String) {
        val postId = post?.idPublicacion ?: return

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(requireContext())
                    .create(ComentariosApi::class.java)
                
                val request = ComentarioRequest(texto)
                val response = api.comentar(postId, request)
                
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Comentario enviado", Toast.LENGTH_SHORT).show()
                    binding.etNuevoComentario.text.clear()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Error del servidor al comentar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red al enviar comentario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CommentsBottomSheet"
        private const val ARG_POST = "arg_post"

        fun newInstance(post: PublicacionResponse): CommentsBottomSheetFragment {
            val fragment = CommentsBottomSheetFragment()
            val args = Bundle()
            args.putSerializable(ARG_POST, post)
            fragment.arguments = args
            return fragment
        }
    }
}
