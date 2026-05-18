package com.example.minicomanda.ui.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.minicomanda.data.local.entities.Sala
import com.example.minicomanda.databinding.FragmentEditarSalaBinding
import kotlinx.coroutines.launch

class EditarSalaFragment : Fragment() {

    private var _binding: FragmentEditarSalaBinding? = null
    private val binding get() = _binding!!

    private val lobbyViewModel: LobbyViewModel by activityViewModels()
    private var salaActual: Sala? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditarSalaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar datos de la sala actual
        lobbyViewModel.currentSala.observe(viewLifecycleOwner) { sala ->
            if (sala != null) {
                salaActual = sala
                binding.etRoomName.setText(sala.nombre)
                binding.switchPrivate.isChecked = sala.esPrivada
                binding.tilPassword.visibility = if (sala.esPrivada) View.VISIBLE else View.GONE
                binding.etPassword.setText("")  // nunca mostramos la contraseña guardada
            }
        }

        // Mostrar/ocultar campo de contraseña
        binding.switchPrivate.setOnCheckedChangeListener { _, isChecked ->
            binding.tilPassword.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) binding.etPassword.text?.clear()
        }

        // Botón guardar cambios
        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etRoomName.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val esPrivada = binding.switchPrivate.isChecked
            val nuevaContrasena = if (esPrivada) {
                binding.etPassword.text.toString().trim().ifBlank { null }
            } else null

            // Actualizar la sala a través del ViewModel
            lobbyViewModel.actualizarSala(nombre, esPrivada, nuevaContrasena)
            Toast.makeText(requireContext(), "Sala actualizada", Toast.LENGTH_SHORT).show()
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