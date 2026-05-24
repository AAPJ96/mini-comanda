package com.example.minicomanda.ui.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.minicomanda.databinding.FragmentUnirseSalaBinding
import kotlinx.coroutines.launch

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
                binding.etRoomId.error = "Ingresa el ID de la sala"
                return@setOnClickListener
            }

            // LÓGICA DE ESTADOS: Verificamos si la caja de contraseña ya está visible
            if (binding.tilPassword.visibility == View.GONE) {

                // ==========================================
                // FASE 1: BUSCAR LA SALA EN ROOM
                // ==========================================
                viewLifecycleOwner.lifecycleScope.launch {
                    // Le pedimos al ViewModel que busque la sala en Room de forma síncrona/suspendida
                    val sala = lobbyViewModel.obtenerSalaPorIdLocal(id)

                    if (sala == null) {
                        // Si Room devuelve null, la sala no existe localmente
                        Toast.makeText(requireContext(), "La sala no existe", Toast.LENGTH_SHORT).show()
                    } else {
                        // Si la sala existe, revisamos si requiere contraseña
                        // Ajusta 'esPrivada' o 'contrasena' según los nombres exactos de tu entidad Sala
                        if (sala.esPrivada || !sala.contrasena.isNullOrEmpty()) {
                            // Si es privada, revelamos la caja de texto
                            binding.tilPassword.visibility = View.VISIBLE
                            binding.etPassword.requestFocus()
                            binding.btnJoin.text = "Confirmar y Unirse"
                        } else {
                            // Si es pública, nos unimos directamente sin pedir contraseña
                            lobbyViewModel.unirseASala(id, null)
                            parentFragmentManager.popBackStack()
                        }
                    }
                }

            } else {
                // ==========================================
                // FASE 2: VALIDAR CONTRASEÑA E INICIAR SESIÓN
                // ==========================================
                val password = binding.etPassword.text.toString().trim()

                if (password.isEmpty()) {
                    binding.etPassword.error = "Esta sala requiere contraseña"
                    return@setOnClickListener
                }

                // El ViewModel se encarga de validar si coincide y guardarlo en SharedPreferences
                viewLifecycleOwner.lifecycleScope.launch {

                    // Ahora podemos usar la función suspendida y esperar su respuesta
                    val exito = lobbyViewModel.unirseASala(id, password)

                    if (exito) {
                        Toast.makeText(requireContext(), "¡Conectado a la sala!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        binding.etPassword.error = "Contraseña incorrecta"
                    }
                }
            }
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