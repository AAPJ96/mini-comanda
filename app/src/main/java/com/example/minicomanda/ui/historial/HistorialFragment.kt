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
import com.example.minicomanda.data.local.entities.ItemComandaConMenu

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistorialViewModel by viewModels()

    private lateinit var adapterPagadas: ComandasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Listener para el dropdown
        binding.spinnerAgrupacion.setOnItemClickListener { _, _, position, _ ->
            val seleccion = agrupaciones[position] // "Diario", "Mensual" o "Anual"
            viewModel.cambiarAgrupacion(seleccion)
        }

        // Configurar botones de alternancia
        binding.btnResumen.setOnClickListener {
            binding.layoutResumen.visibility = View.VISIBLE
            binding.recyclerPagadas.visibility = View.GONE
        }

        binding.btnComandasPagadas.setOnClickListener {
            binding.layoutResumen.visibility = View.GONE
            binding.recyclerPagadas.visibility = View.VISIBLE
        }

        // Configurar RecyclerView de comandas pagadas
        binding.recyclerPagadas.layoutManager = LinearLayoutManager(requireContext())
        adapterPagadas = ComandasAdapter(
            comandas = emptyList(),
            detallesPorComanda = emptyMap<String, List<ItemComandaConMenu>>(), // <-- Especificamos el tipo explícito
            onEditClick = {}, // no se necesita
            mostrarEditar = false
        )
        binding.recyclerPagadas.adapter = adapterPagadas

        // Observar datos del ViewModel
        viewModel.comandasPagadas.observe(viewLifecycleOwner) { comandas ->
            val detalles = viewModel.detalles.value ?: emptyMap<String, List<ItemComandaConMenu>>() // <-- Especificamos el tipo explícito
            adapterPagadas.updateData(comandas, detalles)
        }

        viewModel.detalles.observe(viewLifecycleOwner) { detalles ->
            val comandas = viewModel.comandasPagadas.value ?: emptyList()
            adapterPagadas.updateData(comandas, detalles)
        }

        viewModel.resumenHtml.observe(viewLifecycleOwner) { textoHtml ->
            if (!textoHtml.isNullOrEmpty()) {
                // Traducimos el HTML y lo inyectamos al TextView del resumen
                binding.tvContenidoResumen.text = androidx.core.text.HtmlCompat.fromHtml(
                    textoHtml,
                    androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}