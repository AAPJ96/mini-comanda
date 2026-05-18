package com.example.minicomanda.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.data.local.entities.ItemMenu
import com.example.minicomanda.databinding.FragmentEditarItemBinding

class EditarItemFragment : Fragment() {

    private var _binding: FragmentEditarItemBinding? = null
    private val binding get() = _binding!!

    private val menuViewModel: MenuViewModel by activityViewModels()

    private var itemOriginal: ItemMenu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditarItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ítem de los argumentos (suponemos que se pasa como Serializable)
        itemOriginal = arguments?.getSerializable("item") as? ItemMenu
        if (itemOriginal == null) {
            Toast.makeText(requireContext(), "Error al cargar el ítem", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        val item = itemOriginal!!
        binding.etNombre.setText(item.nombre)
        binding.etPrecio.setText(String.format("%.2f", item.precio / 100.0))
        binding.etDescripcion.setText(item.descripcion ?: "")
        binding.etCategoria.setText(item.categoria ?: "")
        binding.switchModificador.isChecked = item.esModificador

        binding.btnGuardar.setOnClickListener {
            val nuevoNombre = binding.etNombre.text.toString().trim()
            val precioStr = binding.etPrecio.text.toString().trim()
            val nuevaDescripcion = binding.etDescripcion.text.toString().trim()
            val nuevaCategoria = binding.etCategoria.text.toString().trim()
            val esModificador = binding.switchModificador.isChecked

            if (nuevoNombre.isEmpty()) {
                binding.etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            val precioDouble = precioStr.toDoubleOrNull()
            if (precioDouble == null || precioDouble <= 0) {
                binding.etPrecio.error = "Ingresa un precio válido"
                return@setOnClickListener
            }
            val precioCentavos = (precioDouble * 100).toLong()

            val itemActualizado = item.copy(
                nombre = nuevoNombre,
                precio = precioCentavos,
                descripcion = nuevaDescripcion.ifBlank { null },
                categoria = nuevaCategoria.ifBlank { null },
                esModificador = esModificador,
                fechaModificacion = System.currentTimeMillis(),
                sincronizado = false
            )

            menuViewModel.actualizarItem(itemActualizado)
            Toast.makeText(requireContext(), "Item actualizado", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        binding.btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSeleccionarFoto.setOnClickListener {
            Toast.makeText(requireContext(), "Selección de foto (próximamente)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(item: ItemMenu): EditarItemFragment {
            val fragment = EditarItemFragment()
            val args = Bundle().apply {
                putSerializable("item", item)
            }
            fragment.arguments = args
            return fragment
        }
    }
}