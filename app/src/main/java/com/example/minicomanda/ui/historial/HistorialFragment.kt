package com.example.minicomanda.ui.historial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentHistorialBinding
import com.example.minicomanda.ui.comandas.ComandasAdapter

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistorialViewModel by viewModels()

    private lateinit var adapterPagadas: ComandasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar dropdown de agrupación
        val agrupaciones = listOf("Diario", "Mensual", "Anual")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, agrupaciones)
        (binding.tilAgrupacion.editText as? AutoCompleteTextView)?.setAdapter(spinnerAdapter)
        binding.spinnerAgrupacion.setText("Diario", false) // valor por defecto

        // Listener para el dropdown (placeholder)
        binding.spinnerAgrupacion.setOnItemClickListener { _, _, _, _ ->
            Toast.makeText(requireContext(), "Cambio de agrupación (próximamente)", Toast.LENGTH_SHORT).show()
        }

        // Configurar botones de alternancia
        binding.btnResumen.setOnClickListener {
            binding.layoutResumen.visibility = View.VISIBLE
            binding.recyclerPagadas.visibility = View.GONE
            // Resaltar botón seleccionado (opcional, aquí solo usamos Toast)
        }

        binding.btnComandasPagadas.setOnClickListener {
            binding.layoutResumen.visibility = View.GONE
            binding.recyclerPagadas.visibility = View.VISIBLE
        }

        // Configurar RecyclerView de comandas pagadas
        binding.recyclerPagadas.layoutManager = LinearLayoutManager(requireContext())
        adapterPagadas = ComandasAdapter(
            comandas = emptyList(),
            detallesPorComanda = emptyMap(),
            onEditClick = {}, // no se necesita
            mostrarEditar = false
        )
        binding.recyclerPagadas.adapter = adapterPagadas

        // Observar datos del ViewModel
        viewModel.comandasPagadas.observe(viewLifecycleOwner) { comandas ->
            val detalles = viewModel.detalles.value ?: emptyMap()
            adapterPagadas.updateData(comandas, detalles)
        }

        viewModel.detalles.observe(viewLifecycleOwner) { detalles ->
            val comandas = viewModel.comandasPagadas.value ?: emptyList()
            adapterPagadas.updateData(comandas, detalles)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}