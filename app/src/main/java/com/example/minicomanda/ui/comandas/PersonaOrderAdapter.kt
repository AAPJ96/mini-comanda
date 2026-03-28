package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.MenuItem
import com.example.minicomanda.data.local.entities.PedidoDetalle

class PersonaOrderAdapter(
    private val personas: List<String>,
    private val pedidosPorPersona: Map<Int, Map<Int, PedidoDetalle>>,
    private val menuItems: List<MenuItem>,
    private val onIncrement: (Int, MenuItem) -> Unit,
    private val onDecrement: (Int, MenuItem) -> Unit,
    private val onDeletePersona: (Int) -> Unit
) : RecyclerView.Adapter<PersonaOrderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPersonaNombre: TextView = itemView.findViewById(R.id.tv_persona_nombre)
        val layoutItems: LinearLayout = itemView.findViewById(R.id.layout_items)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tv_subtotal)
        val btnDeletePersona: View = itemView.findViewById(R.id.btn_delete_persona)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_persona_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val persona = personas[position]
        val pedidos = pedidosPorPersona[position] ?: emptyMap()

        holder.tvPersonaNombre.text = persona
        holder.btnDeletePersona.setOnClickListener { onDeletePersona(position) }

        // Limpiar items previos
        holder.layoutItems.removeAllViews()

        var subtotal = 0.0
        // Mostrar cada item de esta persona
        for ((itemId, detalle) in pedidos) {
            val menuItem = menuItems.find { it.id == itemId } ?: continue
            subtotal += detalle.cantidad * detalle.precioUnitario

            // Inflar vista para cada item
            val itemView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.item_persona_detalle, holder.layoutItems, false)
            val tvNombre = itemView.findViewById<TextView>(R.id.tv_nombre)
            val tvCantidad = itemView.findViewById<TextView>(R.id.tv_cantidad)
            val tvPrecioUnitario = itemView.findViewById<TextView>(R.id.tv_precio_unitario)
            val tvTotal = itemView.findViewById<TextView>(R.id.tv_total)
            val btnIncrement = itemView.findViewById<View>(R.id.btn_increment)
            val btnDecrement = itemView.findViewById<View>(R.id.btn_decrement)

            tvNombre.text = menuItem.nombre
            tvCantidad.text = detalle.cantidad.toString()
            tvPrecioUnitario.text = "$${"%.2f".format(detalle.precioUnitario)}"
            tvTotal.text = "$${"%.2f".format(detalle.cantidad * detalle.precioUnitario)}"

            btnIncrement.setOnClickListener { onIncrement(position, menuItem) }
            btnDecrement.setOnClickListener { onDecrement(position, menuItem) }

            holder.layoutItems.addView(itemView)
        }

        holder.tvSubtotal.text = "Subtotal: $${"%.2f".format(subtotal)}"
    }

    override fun getItemCount() = personas.size
}