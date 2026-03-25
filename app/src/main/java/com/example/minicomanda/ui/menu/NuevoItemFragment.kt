package com.example.minicomanda.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.minicomanda.data.local.entities.MenuItem
import com.example.minicomanda.databinding.FragmentNuevoItemBinding

class NuevoItemFragment : Fragment() {

    private var _binding: FragmentNuevoItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNuevoItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón agregar
        binding.btnAgregar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val precioStr = binding.etPrecio.text.toString().trim()
            val extras = binding.etExtras.text.toString().trim()

            if (nombre.isEmpty()) {
                binding.etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            if (precioStr.isEmpty()) {
                binding.etPrecio.error = "El precio es obligatorio"
                return@setOnClickListener
            }

            val precio = precioStr.toDoubleOrNull()
            if (precio == null || precio <= 0) {
                binding.etPrecio.error = "Ingresa un precio válido (ej. 25.50)"
                return@setOnClickListener
            }

            // Crear objeto MenuItem (id = 0 para que Room genere automático)
            val nuevoItem = MenuItem(
                nombre = nombre,
                precio = precio,
                foto = null,      // por ahora null
                extras = extras
            )

            // Aquí luego insertaremos en Room. Por ahora solo mostramos un mensaje.
            Toast.makeText(requireContext(), "Item agregado: $nombre", Toast.LENGTH_SHORT).show()

            // Regresar a la pantalla de menú
            parentFragmentManager.popBackStack()
        }

        // Botón cancelar
        binding.btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Botón seleccionar foto (funcionalidad futura)
        binding.btnSeleccionarFoto.setOnClickListener {
            Toast.makeText(requireContext(), "Selección de foto (próximamente)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}