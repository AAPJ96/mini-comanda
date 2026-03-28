package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.MenuItem
import com.example.minicomanda.databinding.ItemMenuGridBinding

class MenuGridAdapter(
    private val items: List<MenuItem>,
    private val onItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuGridAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMenuGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMenuGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvNombre.text = item.nombre
        holder.binding.tvPrecio.text = "$${"%.2f".format(item.precio)}"
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}