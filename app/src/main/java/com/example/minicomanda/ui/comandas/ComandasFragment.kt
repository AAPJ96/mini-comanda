package com.example.minicomanda.ui.comandas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.PedidoDetalle
import com.example.minicomanda.databinding.FragmentComandasBinding

class ComandasFragment : Fragment() {

    private var _binding: FragmentComandasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ComandasViewModel by viewModels()
    private lateinit var adapter: ComandasAdapter

    var currentComandas: List<Comanda>? = null
    var currentDetalles: Map<Int, List<PedidoDetalle>>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentComandasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ComandasAdapter(
            comandas = emptyList(),
            detallesPorComanda = emptyMap(),
            onEditClick = { comanda ->
                Toast.makeText(requireContext(), "Editar: ${comanda.folio}", Toast.LENGTH_SHORT).show()
                // Navegar a fragmento de edición (por hacer)
            },
            onDeleteClick = { comanda ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar comanda")
                    .setMessage("¿Eliminar la comanda ${comanda.folio}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.deleteComanda(comanda)
                        Toast.makeText(requireContext(), "Comanda eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerView.adapter = adapter

        // Observar datos
        viewModel.comandas.observe(viewLifecycleOwner) { comandas ->
            currentComandas = comandas
            // Enviamos las comandas y usamos los detalles actuales o un mapa vacío si aún no llegan
            adapter.updateData(comandas, currentDetalles ?: emptyMap())
        }

        viewModel.detalles.observe(viewLifecycleOwner) { detalles ->
            currentDetalles = detalles
            // Enviamos los detalles y usamos las comandas actuales o una lista vacía
            adapter.updateData(currentComandas ?: emptyList(), detalles)
        }

        // FAB para agregar
        binding.fabAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Agregar comanda (próximamente)", Toast.LENGTH_SHORT).show()
            // Navegar a fragmento de nueva comanda
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}