package com.example.minicomanda.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.minicomanda.ui.menu.MenuAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.MenuItem
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

        // Configurar adaptador
        adapter = MenuAdapter(
            items = mutableListOf(),
            onEditClick = { item ->
                // AQUÍ VA TU CÓDIGO DE NAVEGACIÓN [cite: 44]
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
                        // Eliminar del ViewModel
                        viewModel.removeMenuItem(item)
                        Toast.makeText(requireContext(), "Item eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerView.adapter = adapter

        // Observar cambios en la lista
        viewModel.menuItems.observe(viewLifecycleOwner) { items ->
            adapter.updateList(items)
        }

        // Cargar datos iniciales si está vacío
        if (viewModel.menuItems.value.isNullOrEmpty()) {
            loadDummyData()
        }

        // Configurar drag & drop
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                viewModel.moveItem(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        // FAB para agregar
        binding.fabAdd.setOnClickListener {
            val newItemFragment = NuevoItemFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, newItemFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadDummyData() {
        val dummyList = listOf(
            MenuItem(1, "Taco Carne Maíz", 25.0),
            MenuItem(2, "Taco Carne Harina", 35.0),
            MenuItem(3, "Taco Papa Maiz", 30.0),
            MenuItem(4, "Taco Papa Harina", 45.0)
        )
        viewModel.setMenuItems(dummyList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}