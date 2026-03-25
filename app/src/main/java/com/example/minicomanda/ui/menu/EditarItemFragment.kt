package com.example.minicomanda.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.data.local.entities.MenuItem
import com.example.minicomanda.databinding.FragmentEditarItemBinding

class EditarItemFragment : Fragment() {

    private var _binding: FragmentEditarItemBinding? = null
    private val binding get() = _binding!!

    private val menuViewModel: MenuViewModel by activityViewModels()

    private var itemId: Int = 0
    private var originalNombre: String = ""
    private var originalPrecio: Double = 0.0
    private var originalExtras: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditarItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el item de los argumentos
        arguments?.let {
            itemId = it.getInt("item_id", 0)
            originalNombre = it.getString("item_nombre", "")
            originalPrecio = it.getDouble("item_precio", 0.0)
            originalExtras = it.getString("item_extras")
        }

        // Prellenar campos
        binding.etNombre.setText(originalNombre)
        binding.etPrecio.setText(String.format("%.2f", originalPrecio))
        binding.etExtras.setText(originalExtras ?: "")

        // Botón guardar cambios
        binding.btnGuardar.setOnClickListener {
            val nuevoNombre = binding.etNombre.text.toString().trim()
            val precioStr = binding.etPrecio.text.toString().trim()
            val nuevosExtras = binding.etExtras.text.toString().trim()

            if (nuevoNombre.isEmpty()) {
                binding.etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            if (precioStr.isEmpty()) {
                binding.etPrecio.error = "El precio es obligatorio"
                return@setOnClickListener
            }

            val nuevoPrecio = precioStr.toDoubleOrNull()
            if (nuevoPrecio == null || nuevoPrecio <= 0) {
                binding.etPrecio.error = "Ingresa un precio válido (ej. 25.50)"
                return@setOnClickListener
            }

            // Crear el ítem actualizado (mantenemos el mismo id y foto)
            val itemActualizado = MenuItem(
                id = itemId,
                nombre = nuevoNombre,
                precio = nuevoPrecio,
                foto = null, // más adelante se manejará la foto
                extras = nuevosExtras
            )

            // Actualizar en ViewModel
            menuViewModel.updateMenuItem(itemActualizado)

            Toast.makeText(requireContext(), "Item actualizado", Toast.LENGTH_SHORT).show()

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

    companion object {
        fun newInstance(item: MenuItem): EditarItemFragment {
            val fragment = EditarItemFragment()
            val args = Bundle().apply {
                putInt("item_id", item.id)
                putString("item_nombre", item.nombre)
                putDouble("item_precio", item.precio)
                putString("item_extras", item.extras)
            }
            fragment.arguments = args
            return fragment
        }
    }
}