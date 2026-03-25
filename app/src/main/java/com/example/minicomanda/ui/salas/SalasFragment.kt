package com.example.minicomanda.ui.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentSalasBinding

class SalasFragment : Fragment() {

    private var _binding: FragmentSalasBinding? = null
    private val binding get() = _binding!!

    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSalasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observar cambios en la sala actual
        lobbyViewModel.currentLobby.observe(viewLifecycleOwner) { lobby ->
            if (lobby != null) {
                // Mostrar información de la sala activa
                binding.cardRoomInfo.visibility = View.VISIBLE
                binding.layoutActions.visibility = View.GONE

                binding.tvRoomIdValue.text = "ID: ${lobby.lobbyId}"
                binding.tvRoomNameValue.text = "Nombre: ${lobby.lobbyName.takeIf { it.isNotEmpty() } ?: "Sin nombre"}"
            } else {
                // Mostrar opciones de crear/unirse
                binding.cardRoomInfo.visibility = View.GONE
                binding.layoutActions.visibility = View.VISIBLE
            }
        }

        // Click en "Crear Sala"
        binding.cardCreate.setOnClickListener {
            val crearFragment = CrearSalaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, crearFragment)
                .addToBackStack(null)
                .commit()
        }

        // Click en "Unirse a Sala"
        binding.cardJoin.setOnClickListener {
            val unirseFragment = UnirseSalaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, unirseFragment)
                .addToBackStack(null)
                .commit()
        }

        // Botón Salir de sala
        binding.btnLeaveRoom.setOnClickListener {
            lobbyViewModel.leaveLobby()
            Toast.makeText(requireContext(), "Has salido de la sala", Toast.LENGTH_SHORT).show()
        }

        // Botón Editar sala (funcionalidad futura)
        binding.btnEditRoom.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}