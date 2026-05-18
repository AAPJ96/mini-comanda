package com.example.minicomanda.ui.cocina

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minicomanda.databinding.FragmentCocinaBinding

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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}