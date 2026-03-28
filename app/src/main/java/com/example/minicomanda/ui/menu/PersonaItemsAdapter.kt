package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.MenuItem
import com.example.minicomanda.data.local.entities.PedidoDetalle
import com.example.minicomanda.databinding.ItemPersonaDetalleBinding

class PersonaItemsAdapter(
    private val items: List<Pair<MenuItem, PedidoDetalle>>,
    private val onIncrement: (MenuItem) -> Unit,
    private val onDecrement: (MenuItem) -> Unit
) : RecyclerView.Adapter<PersonaItemsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPersonaDetalleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPersonaDetalleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (menuItem, detalle) = items[position]
        holder.binding.tvNombre.text = menuItem.nombre
        holder.binding.tvCantidad.text = detalle.cantidad.toString()
        holder.binding.tvPrecioUnitario.text = "$${"%.2f".format(detalle.precioUnitario)}"
        holder.binding.tvTotal.text = "$${"%.2f".format(detalle.cantidad * detalle.precioUnitario)}"

        holder.binding.btnIncrement.setOnClickListener { onIncrement(menuItem) }
        holder.binding.btnDecrement.setOnClickListener { onDecrement(menuItem) }
    }

    override fun getItemCount() = items.size
}