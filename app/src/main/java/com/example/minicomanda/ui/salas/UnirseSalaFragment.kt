package com.example.minicomanda.ui.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        binding.btnJoin.setOnClickListener {
            val id = binding.etRoomId.text.toString().trim()
            if (id.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa el ID de la sala", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val password = binding.etPassword.text.toString().trim().ifBlank { null }

            // Llamar al ViewModel para unirse (él hará la validación)
            lobbyViewModel.unirseASala(id, password)

            // Regresar a SalasFragment
            parentFragmentManager.popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}