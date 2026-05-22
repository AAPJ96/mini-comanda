package com.example.minicomanda.ui.cocina

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minicomanda.databinding.FragmentCocinaBinding
import com.example.minicomanda.R

class CocinaFragment : Fragment() {

    private var _binding: FragmentCocinaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CocinaViewModel by viewModels()
    private lateinit var adapter: CocinaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCocinaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewCocina.layoutManager = LinearLayoutManager(requireContext())
        adapter = CocinaAdapter(emptyList()) { index, detalleId, isChecked ->
            viewModel.marcarItemPreparado(index, detalleId, isChecked)
        }
        binding.recyclerViewCocina.adapter = adapter

        viewModel.comandas.observe(viewLifecycleOwner) { comandas ->
            adapter.updateComandas(comandas)
            if (comandas.isEmpty()) {
                binding.recyclerViewCocina.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                // Opcional: Puedes personalizar el mensaje por pantalla
                binding.tvEmptyState.text = "Sin órdenes nuevas.\nCuando lleguen, las verás aparecer aquí."
            } else {
                binding.recyclerViewCocina.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
            }
        }

        // Observamos el evento de comanda completada
        viewModel.eventoComandaCompletada.observe(viewLifecycleOwner) { itemId ->
            if (itemId != null) {

                // 1. Creamos el Snackbar y lo guardamos en una variable
                val snackbar = com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "Comanda completada y removida",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                )

                // 2. Le aplicamos tu color de fondo azul
                snackbar.view.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.blue)
                )

                // 3. Le ponemos el texto del mensaje en blanco
                snackbar.setTextColor(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white)
                )

                // 4. Le ponemos el texto del botón "DESHACER" en blanco (o cámbialo si prefieres otro color para resaltar)
                snackbar.setActionTextColor(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white)
                )

                // 5. Configuramos la acción y lo mostramos
                snackbar.setAction("DESHACER") {
                    viewModel.deshacerItem(itemId)
                }.show()

                // Limpiamos el evento para que no se repita
                viewModel.eventoDeshacerMostrado()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}