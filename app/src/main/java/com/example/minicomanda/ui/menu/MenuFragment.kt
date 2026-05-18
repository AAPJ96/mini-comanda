package com.example.minicomanda.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.ItemMenu
import com.example.minicomanda.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels()
    private lateinit var adapter: MenuAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MenuAdapter(
            items = mutableListOf(),
            onEditClick = { item ->
                val editFragment = EditarItemFragment.newInstance(item)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, editFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { item ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar item")
                    .setMessage("¿Estás seguro de eliminar ${item.nombre}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.eliminarItem(item)
                        Toast.makeText(requireContext(), "Item eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerView.adapter = adapter

        viewModel.menuItems.observe(viewLifecycleOwner) { items ->
            adapter.updateList(items)
        }

        // Drag & drop
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                viewModel.moverItem(fromPos, toPos)
                adapter.moveItem(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.fabAdd.setOnClickListener {
            val newItemFragment = NuevoItemFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, newItemFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}