package com.example.minicomanda.ui.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentUnirseSalaBinding

class UnirseSalaFragment : Fragment() {

    private var _binding: FragmentUnirseSalaBinding? = null
    private val binding get() = _binding!!

    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentUnirseSalaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar campo de contraseña solo si se ingresa un ID que indique que es privada?
        // Por ahora lo dejamos visible siempre (el usuario decide si pone contraseña)
        // Más adelante podríamos consultar al servidor si la sala requiere contraseña

        // Botón unirse
        binding.btnJoin.setOnClickListener {
            val lobbyId = binding.etRoomId.text.toString().trim()
            if (lobbyId.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa el ID de la sala", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val password = binding.etPassword.text.toString().trim()

            // Simulación: buscar sala (aquí iría la llamada a la API)
            // Por ahora creamos un objeto Lobby con datos ficticios
            val joinedLobby = Lobby(
                lobbyId = lobbyId,
                lobbyName = "Sala $lobbyId",
                isPrivate = password.isNotEmpty(),
                password = password
            )

            // Unirse
            lobbyViewModel.joinLobby(joinedLobby)
            Toast.makeText(requireContext(), "Te has unido a la sala $lobbyId", Toast.LENGTH_SHORT).show()

            // Regresar a SalasFragment
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