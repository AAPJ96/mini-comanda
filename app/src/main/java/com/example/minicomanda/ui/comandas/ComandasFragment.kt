package com.example.minicomanda.ui.comandas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentComandasBinding

class ComandasFragment : Fragment() {

    private var _binding: FragmentComandasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ComandasViewModel by viewModels()
    private lateinit var adapter: ComandasAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentComandasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ComandasAdapter(
            comandas = emptyList(),
            detallesPorComanda = emptyMap(),
            onEditClick = { comanda ->
                val editarFragment = EditarComandaFragment.newInstance(comanda.id)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, editarFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        binding.recyclerView.adapter = adapter

        // Observar comandas y detalles y actualizar el adaptador cuando cualquiera cambie
        viewModel.comandas.observe(viewLifecycleOwner) { comandas ->
            adapter.updateData(comandas, viewModel.detalles.value ?: emptyMap())

            if (comandas.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                // Opcional: Puedes personalizar el mensaje por pantalla
                binding.tvEmptyState.text = "No hay comandas activas en este momento. Usa el botón '+' para agregar comandas."
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
            }
        }

        viewModel.detalles.observe(viewLifecycleOwner) { detalles ->
            adapter.updateData(viewModel.comandas.value ?: emptyList(), detalles)
        }

        binding.fabAdd.setOnClickListener {
            val agregarFragment = AgregarComandaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, agregarFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}