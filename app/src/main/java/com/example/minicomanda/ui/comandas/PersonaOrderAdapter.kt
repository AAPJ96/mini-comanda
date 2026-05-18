package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.ItemMenu
import com.example.minicomanda.data.local.entities.ItemComanda

class PersonaOrderAdapter(
    private val personas: List<String>,
    private val pedidosPorPersona: Map<Int, Map<String, ItemComanda>>,  // índice persona -> (itemMenuId -> ItemComanda)
    private val menuItems: List<ItemMenu>,  // lista completa del menú para obtener nombres
    private val onIncrement: (Int, ItemMenu) -> Unit,
    private val onDecrement: (Int, ItemMenu) -> Unit,
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

        holder.layoutItems.removeAllViews()
        var subtotalCentavos = 0L

        for ((itemMenuId, itemComanda) in pedidos) {
            val menuItem = menuItems.find { it.id == itemMenuId } ?: continue
            subtotalCentavos += itemComanda.cantidad * itemComanda.precioOriginalUnidad

            val itemView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.item_persona_detalle, holder.layoutItems, false)
            val tvNombre = itemView.findViewById<TextView>(R.id.tv_nombre)
            val tvCantidad = itemView.findViewById<TextView>(R.id.tv_cantidad)
            val tvPrecioUnitario = itemView.findViewById<TextView>(R.id.tv_precio_unitario)
            val tvTotal = itemView.findViewById<TextView>(R.id.tv_total)
            val btnIncrement = itemView.findViewById<View>(R.id.btn_increment)
            val btnDecrement = itemView.findViewById<View>(R.id.btn_decrement)

            tvNombre.text = menuItem.nombre
            tvCantidad.text = itemComanda.cantidad.toString()
            tvPrecioUnitario.text = "$${"%.2f".format(itemComanda.precioOriginalUnidad / 100.0)}"
            tvTotal.text = "$${"%.2f".format(itemComanda.cantidad * itemComanda.precioOriginalUnidad / 100.0)}"

            btnIncrement.setOnClickListener { onIncrement(position, menuItem) }
            btnDecrement.setOnClickListener { onDecrement(position, menuItem) }

            holder.layoutItems.addView(itemView)
        }

        holder.tvSubtotal.text = "Subtotal: $${"%.2f".format(subtotalCentavos / 100.0)}"
    }

    override fun getItemCount() = personas.size
}