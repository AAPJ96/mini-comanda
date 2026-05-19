package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ItemComandaConMenu // <-- Importación actualizada
import java.text.SimpleDateFormat
import java.util.*

class ComandasAdapter(
    private var comandas: List<Comanda>,
    private var detallesPorComanda: Map<String, List<ItemComandaConMenu>>,  // <-- Tipo actualizado
    private val onEditClick: (Comanda) -> Unit,
    private val mostrarEditar: Boolean = true
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

        holder.tvFolio.text = comanda.folio?.toString() ?: "Sin folio"
        holder.tvFecha.text = dateFormat.format(Date(comanda.fechaCreacion))
        holder.tvEstado.text = comanda.estado
        holder.tvCliente.text = comanda.comensal ?: ""
        holder.tvTipoPedido.text = if (comanda.esParaLlevar) "Para llevar" else "Comer aquí"

        holder.layoutDetalles.removeAllViews()
        buildDetalleView(holder.layoutDetalles, comanda, detalles)

        holder.btnEditar.visibility = if (mostrarEditar) View.VISIBLE else View.GONE
        holder.btnEditar.setOnClickListener { onEditClick(comanda) }
    }

    // <-- Parámetro actualizado
    private fun buildDetalleView(parent: LinearLayout, comanda: Comanda, detalles: List<ItemComandaConMenu>) {
        if (detalles.isEmpty()) {
            val tvEmpty = TextView(parent.context).apply {
                text = "Sin detalles"
                setPadding(0, 8, 0, 8)
            }
            parent.addView(tvEmpty)
            return
        }

        // Agrupar por persona leyendo los datos anidados
        val porPersona = detalles.groupBy { it.itemComanda.persona }
        for ((persona, items) in porPersona) {
            val personaLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_persona, parent, false) as LinearLayout
            val tvPersonaNombre = personaLayout.findViewById<TextView>(R.id.tv_persona_nombre)
            val tvPersonaSubtotal = personaLayout.findViewById<TextView>(R.id.tv_persona_subtotal)
            val layoutItems = personaLayout.findViewById<LinearLayout>(R.id.layout_items)

            // Suma de centavos leyendo los datos anidados
            val subtotalCentavos = items.sumOf { it.itemComanda.cantidad * it.itemComanda.precioOriginalUnidad }
            tvPersonaNombre.text = "Persona $persona"
            tvPersonaSubtotal.text = "$${"%.2f".format(subtotalCentavos / 100.0)}"

            for (item in items) {
                val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle, layoutItems, false)
                val tvCantidad = itemLayout.findViewById<TextView>(R.id.tv_cantidad)
                val tvNombre = itemLayout.findViewById<TextView>(R.id.tv_nombre)
                val tvTotal = itemLayout.findViewById<TextView>(R.id.tv_total)

                // Extraemos ambas partes de la relación
                val pedido = item.itemComanda
                val menu = item.itemMenu

                tvCantidad.text = "${pedido.cantidad} x"

                // ¡Magia! Ya tenemos el nombre real sin hacer mapas extraños
                tvNombre.text = "${menu.nombre} ($${"%.2f".format(pedido.precioOriginalUnidad / 100.0)})"
                tvTotal.text = "$${"%.2f".format(pedido.cantidad * pedido.precioOriginalUnidad / 100.0)}"

                layoutItems.addView(itemLayout)
            }
            parent.addView(personaLayout)
        }

        val totalGeneralCentavos = detalles.sumOf { it.itemComanda.cantidad * it.itemComanda.precioOriginalUnidad }
        if (totalGeneralCentavos > 0) {
            val totalView = LayoutInflater.from(parent.context).inflate(R.layout.item_total_row, parent, false)
            val tvTotalValue = totalView.findViewById<TextView>(R.id.tv_total_value)
            tvTotalValue.text = "$${"%.2f".format(totalGeneralCentavos / 100.0)}"
            parent.addView(totalView)
        }
    }

    override fun getItemCount() = comandas.size

    // <-- Parámetro actualizado
    fun updateData(newComandas: List<Comanda>, newDetalles: Map<String, List<ItemComandaConMenu>>) {
        comandas = newComandas
        detallesPorComanda = newDetalles
        notifyDataSetChanged()
    }
}