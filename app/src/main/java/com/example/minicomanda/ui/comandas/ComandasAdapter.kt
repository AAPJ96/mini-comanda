package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.PedidoDetalle
import java.text.SimpleDateFormat
import java.util.*

class ComandasAdapter(
    private var comandas: List<Comanda>,
    private var detallesPorComanda: Map<Int, List<PedidoDetalle>>,
    private val onEditClick: (Comanda) -> Unit
) : RecyclerView.Adapter<ComandasAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFolio: TextView = itemView.findViewById(R.id.tv_folio)
        val tvFecha: TextView = itemView.findViewById(R.id.tv_fecha)
        val tvEstado: TextView = itemView.findViewById(R.id.tv_estado)
        val tvCliente: TextView = itemView.findViewById(R.id.tv_cliente)
        val tvTipoPedido: TextView = itemView.findViewById(R.id.tv_tipo_pedido)
        val layoutDetalles: LinearLayout = itemView.findViewById(R.id.layout_detalles)
        val btnEditar: View = itemView.findViewById(R.id.btn_editar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comanda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comanda = comandas[position]
        val detalles = detallesPorComanda[comanda.id] ?: emptyList()

        holder.tvFolio.text = comanda.folio
        holder.tvFecha.text = dateFormat.format(comanda.fecha)
        holder.tvEstado.text = comanda.estado
        holder.tvCliente.text = comanda.nombreCliente
        holder.tvTipoPedido.text = if (comanda.paraLlevar) "Para llevar" else "Comer aquí"

        // Limpiar y construir detalles dinámicamente
        holder.layoutDetalles.removeAllViews()
        buildDetalleView(holder.layoutDetalles, comanda, detalles)

        holder.btnEditar.setOnClickListener { onEditClick(comanda) }
    }

    override fun getItemCount() = comandas.size

    fun updateData(newComandas: List<Comanda>, newDetalles: Map<Int, List<PedidoDetalle>>) {
        comandas = newComandas
        detallesPorComanda = newDetalles
        notifyDataSetChanged()
    }

    private fun buildDetalleView(parent: LinearLayout, comanda: Comanda, detalles: List<PedidoDetalle>) {
        if (detalles.isEmpty()) {
            val tvEmpty = TextView(parent.context).apply {
                text = "Sin detalles"
                setPadding(0, 8, 0, 8)
            }
            parent.addView(tvEmpty)
            return
        }

        // Agrupar por persona
        val personas = detalles.groupBy { it.persona }
        for ((persona, items) in personas) {
            val personaLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_persona, parent, false) as LinearLayout
            val tvPersonaNombre = personaLayout.findViewById<TextView>(R.id.tv_persona_nombre)
            val tvPersonaSubtotal = personaLayout.findViewById<TextView>(R.id.tv_persona_subtotal)
            val layoutItems = personaLayout.findViewById<LinearLayout>(R.id.layout_items)

            val subtotal = items.sumOf { it.cantidad * it.precioUnitario }
            tvPersonaNombre.text = persona
            tvPersonaSubtotal.text = "$${"%.2f".format(subtotal)}"

            // Agregar items de esta persona
            for (item in items) {
                val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle, layoutItems, false)

                // Referencias a los componentes según tu nuevo XML
                val tvCantidad = itemLayout.findViewById<TextView>(R.id.tv_cantidad)
                val tvNombre = itemLayout.findViewById<TextView>(R.id.tv_nombre) // Este ahora incluye el precio
                val tvTotal = itemLayout.findViewById<TextView>(R.id.tv_total)

                // 1. Asignar la cantidad
                tvCantidad.text = "${item.cantidad} x"

                // 2. Combinar nombre y precio unitario en el mismo TextView
                val nombreProducto = getItemNombre(item.itemMenuId)
                val precioFormateado = "($${"%.2f".format(item.precioUnitario)})"
                tvNombre.text = "$nombreProducto $precioFormateado"

                // 3. Asignar el total de ese renglón
                tvTotal.text = "$${"%.2f".format(item.cantidad * item.precioUnitario)}"

                layoutItems.addView(itemLayout)
            }

            parent.addView(personaLayout)
        }

        val totalGeneral = detalles.sumOf { it.cantidad * it.precioUnitario }
        if (totalGeneral > 0) {
            val totalView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_total_row, parent, false)
            val tvTotalValue = totalView.findViewById<TextView>(R.id.tv_total_value)
            tvTotalValue.text = "$${"%.2f".format(totalGeneral)}"
            parent.addView(totalView)
        }
    }

    private fun getItemNombre(itemMenuId: Int): String {
        return mapOf(
            1 to "Taco Carne Maíz",
            2 to "Taco Carne Harina",
            3 to "Taco Papa Maíz",
            4 to "Taco Papa Harina"
        )[itemMenuId] ?: "Item $itemMenuId"
    }
}