package com.example.minicomanda.ui.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentCrearSalaBinding

class CrearSalaFragment : Fragment() {

    private var _binding: FragmentCrearSalaBinding? = null
    private val binding get() = _binding!!

    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCrearSalaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar/ocultar campo de contraseña según el switch
        binding.switchPrivate.setOnCheckedChangeListener { _, isChecked ->
            binding.tilPassword.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) binding.etPassword.text?.clear()
        }

        // Botón crear
        binding.btnCreate.setOnClickListener {
            val name = binding.etRoomName.text.toString().trim()
            val isPrivate = binding.switchPrivate.isChecked
            val password = if (isPrivate) binding.etPassword.text.toString().trim() else ""

            // Generar ID de sala simulado (luego lo hará el servidor)
            val lobbyId = (100000..999999).random().toString()

            val newLobby = Lobby(
                lobbyId = lobbyId,
                lobbyName = name,
                isPrivate = isPrivate,
                password = password
            )

            // Guardar en ViewModel
            lobbyViewModel.joinLobby(newLobby)

            Toast.makeText(requireContext(), "Sala creada: $lobbyId", Toast.LENGTH_LONG).show()

            // Regresar al fragment anterior (SalasFragment)
            parentFragmentManager.popBackStack()
        }

        // Botón cancelar
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}