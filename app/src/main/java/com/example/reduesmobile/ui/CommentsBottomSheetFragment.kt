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
import com.example.reduesmobile.data.auth.TokenManager
import com.example.reduesmobile.data.dto.ComentarioRequest
import com.example.reduesmobile.data.dto.ComentarioResponse
import com.example.reduesmobile.data.dto.PublicacionResponse
import com.example.reduesmobile.databinding.BottomSheetComentariosBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetComentariosBinding? = null
    private val binding get() = _binding!!
    private var post: PublicacionResponse? = null
    private lateinit var tokenManager: TokenManager
    private lateinit var comentarioAdapter: ComentarioAdapter
    private val listaLocalComentarios = mutableListOf<ComentarioResponse>()
    private var isSending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
        post = arguments?.getSerializable(ARG_POST) as? PublicacionResponse
        post?.let { listaLocalComentarios.addAll(it.comentarios) }
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
        
        val miNombre = tokenManager.getUserName() ?: "Usuario"
        binding.tvComentariosTitle.text = "Comentarios"
        binding.etNuevoComentario.hint = "Comentar como $miNombre..."
    }

    private fun setupRecyclerView() {
        comentarioAdapter = ComentarioAdapter(listaLocalComentarios)
        binding.rvComentarios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComentarios.adapter = comentarioAdapter
    }

    private fun setupSendButton() {
        binding.btnEnviarComentario.setOnClickListener {
            if (isSending) return@setOnClickListener
            val texto = binding.etNuevoComentario.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarComentario(texto)
            }
        }
    }

    private fun enviarComentario(texto: String) {
        val postId = post?.idPublicacion ?: return
        val miNombre = tokenManager.getUserName() ?: "Usuario"

        isSending = true
        binding.btnEnviarComentario.isEnabled = false
        binding.btnEnviarComentario.alpha = 0.5f

        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.getRetrofitInstance(requireContext())
                    .create(ComentariosApi::class.java)
                
                val response = api.comentar(postId, ComentarioRequest(texto))
                
                if (response.isSuccessful) {
                    val nuevo = ComentarioResponse(
                        idComentario = 0,
                        idPublicacion = postId,
                        idUsuario = tokenManager.getUserId(),
                        usuario = miNombre,
                        texto = texto,
                        fechaComentario = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    )

                    comentarioAdapter.agregarComentario(nuevo)
                    binding.rvComentarios.scrollToPosition(listaLocalComentarios.size - 1)
                    binding.etNuevoComentario.text.clear()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al enviar", Toast.LENGTH_SHORT).show()
            } finally {
                isSending = false
                binding.btnEnviarComentario.isEnabled = true
                binding.btnEnviarComentario.alpha = 1.0f
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
