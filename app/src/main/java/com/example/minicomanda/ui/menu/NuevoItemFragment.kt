package com.example.minicomanda.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.data.local.entities.ItemMenu
import com.example.minicomanda.databinding.FragmentNuevoItemBinding
import java.util.UUID

class NuevoItemFragment : Fragment() {

    private var _binding: FragmentNuevoItemBinding? = null
    private val binding get() = _binding!!

    private val menuViewModel: MenuViewModel by activityViewModels()

    // 1. Variable para guardar los bytes de la imagen
    private var imagenSeleccionadaBytes: ByteArray? = null

    // 2. Lanzador para abrir la galería y extraer los bytes
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

            val precioCentavos = (precioDouble * 100).toLong()

            val nuevoItem = ItemMenu(
                id = UUID.randomUUID().toString(),
                salaId = obtenerSalaIdActiva(),
                nombre = nombre,
                precio = precioCentavos,
                // 3. Asignamos los bytes en lugar del string
                imagen = imagenSeleccionadaBytes,
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

        // 4. Lanzar el selector pidiendo solo imágenes
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
}