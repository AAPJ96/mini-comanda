package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.data.local.entities.ItemMenu
import com.example.minicomanda.data.local.entities.ItemComanda
import com.example.minicomanda.databinding.ItemPersonaDetalleBinding

class PersonaItemsAdapter(
    private val items: List<Pair<ItemMenu, ItemComanda>>,
    private val onIncrement: (ItemMenu) -> Unit,
    private val onDecrement: (ItemMenu) -> Unit
) : RecyclerView.Adapter<PersonaItemsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPersonaDetalleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPersonaDetalleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (menuItem, itemComanda) = items[position]
        holder.binding.tvNombre.text = "${menuItem.nombre} ($${"%.2f".format(itemComanda.precioOriginalUnidad / 100.0)})"
        holder.binding.tvCantidad.text = itemComanda.cantidad.toString()
        // precioOriginalUnidad está en centavos, dividir entre 100.0 para mostrar
        holder.binding.tvPrecioUnitario.text = "$${"%.2f".format(itemComanda.precioOriginalUnidad / 100.0)}"
        holder.binding.tvTotal.text = "$${"%.2f".format(itemComanda.cantidad * itemComanda.precioOriginalUnidad / 100.0)}"

        holder.binding.btnIncrement.setOnClickListener { onIncrement(menuItem) }
        holder.binding.btnDecrement.setOnClickListener { onDecrement(menuItem) }
    }

    override fun getItemCount() = items.size
}