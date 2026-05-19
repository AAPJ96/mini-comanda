package com.example.minicomanda.ui.menu

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.data.local.entities.ItemMenu
import com.example.minicomanda.databinding.FragmentEditarItemBinding

class EditarItemFragment : Fragment() {

    private var _binding: FragmentEditarItemBinding? = null
    private val binding get() = _binding!!

    private val menuViewModel: MenuViewModel by activityViewModels()

    private var itemOriginal: ItemMenu? = null

    // 1. Variable para guardar los bytes de la imagen
    private var imagenSeleccionadaBytes: ByteArray? = null

    // 2. Lanzador para extraer la nueva foto
    // Lanzador actualizado para extraer y comprimir la foto
    private val seleccionarImagenLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Llamamos a nuestra nueva función procesadora
            val bytesComprimidos = procesarYComprimirImagen(uri)

            if (bytesComprimidos != null) {
                imagenSeleccionadaBytes = bytesComprimidos

                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytesComprimidos, 0, bytesComprimidos.size)
                binding.ivPreview.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditarItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemOriginal = arguments?.getSerializable("item") as? ItemMenu
        if (itemOriginal == null) {
            Toast.makeText(requireContext(), "Error al cargar el ítem", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        val item = itemOriginal!!

        // 3. Cargamos la imagen original en bytes
        imagenSeleccionadaBytes = item.imagen

        item.imagen?.let { bytes ->
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.ivPreview.setImageBitmap(bitmap)
        }

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
                // 4. Sobrescribimos o mantenemos los bytes
                imagen = imagenSeleccionadaBytes,
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

        // 5. Lanzar el selector
        binding.btnSeleccionarFoto.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }
    }

    /**
     * Toma la URI de una imagen, la recorta a un cuadrado centrado,
     * la reduce a 256x256 y la devuelve como un ByteArray ultra ligero.
     */
    private fun procesarYComprimirImagen(uri: android.net.Uri): ByteArray? {
        return try {
            // 1. Obtener la imagen original
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // 2. Calcular las medidas para hacer un recorte cuadrado exacto al centro
            val width = originalBitmap.width
            val height = originalBitmap.height
            val minEdge = Math.min(width, height)

            val xOffset = (width - minEdge) / 2
            val yOffset = (height - minEdge) / 2

            val squareBitmap = android.graphics.Bitmap.createBitmap(
                originalBitmap,
                xOffset,
                yOffset,
                minEdge,
                minEdge
            )

            // 3. Escalar el cuadrado a 256x256 píxeles
            val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(squareBitmap, 256, 256, true)

            // 4. Comprimirla en formato JPEG (calidad 80 es excelente para este tamaño)
            val outputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)

            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
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