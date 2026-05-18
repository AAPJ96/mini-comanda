package com.example.minicomanda.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.data.local.entities.ItemMenu
import com.example.minicomanda.databinding.FragmentNuevoItemBinding
import java.util.UUID

class NuevoItemFragment : Fragment() {

    private var _binding: FragmentNuevoItemBinding? = null
    private val binding get() = _binding!!

    private val menuViewModel: MenuViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNuevoItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun obtenerSalaIdActiva(): String {
        val prefs = requireActivity().getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("sala_id", "") ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAgregar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val precioStr = binding.etPrecio.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val categoria = binding.etCategoria.text.toString().trim()
            val esModificador = binding.switchModificador.isChecked

            if (nombre.isEmpty()) {
                binding.etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            val precioDouble = precioStr.toDoubleOrNull()
            if (precioDouble == null || precioDouble <= 0) {
                binding.etPrecio.error = "Ingresa un precio válido (ej. 25.50)"
                return@setOnClickListener
            }

            // Convertir a centavos (Long)
            val precioCentavos = (precioDouble * 100).toLong()

            val nuevoItem = ItemMenu(
                id = UUID.randomUUID().toString(),
                salaId = obtenerSalaIdActiva(),   // ← ¡agregado!
                nombre = nombre,
                precio = precioCentavos,
                imagen = null,
                descripcion = descripcion.ifBlank { null },
                categoria = categoria.ifBlank { null },
                esModificador = esModificador,
                ordenVisualizacion = 0,
                fechaCreacion = System.currentTimeMillis(),
                fechaModificacion = System.currentTimeMillis(),
                activo = true,
                sincronizado = false
            )

            menuViewModel.agregarItem(nuevoItem)
            Toast.makeText(requireContext(), "Item agregado: $nombre", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        binding.btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Botón de foto (placeholder)
        binding.btnSeleccionarFoto.setOnClickListener {
            Toast.makeText(requireContext(), "Selección de foto (próximamente)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}