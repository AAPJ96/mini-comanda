package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val onEditClick: (Comanda) -> Unit,
    private val onDeleteClick: (Comanda) -> Unit
) : RecyclerView.Adapter<ComandasAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val expandedPositions = mutableSetOf<Int>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFolio: TextView = itemView.findViewById(R.id.tv_folio)
        val tvFecha: TextView = itemView.findViewById(R.id.tv_fecha)
        val tvEstado: TextView = itemView.findViewById(R.id.tv_estado)
        val tvCliente: TextView = itemView.findViewById(R.id.tv_cliente)
        val tvTipoPedido: TextView = itemView.findViewById(R.id.tv_tipo_pedido)
        val layoutDetalles: ViewGroup = itemView.findViewById(R.id.layout_detalles)
        val tvDetalleTexto: TextView = itemView.findViewById(R.id.tv_detalle_texto)
        val btnEditar: View = itemView.findViewById(R.id.btn_editar)
        val btnEliminar: View = itemView.findViewById(R.id.btn_eliminar)
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

        // Formatear desglose
        holder.tvDetalleTexto.text = formatDetalle(comanda, detalles)

        // Toggle colapsado
        holder.itemView.setOnClickListener {
            if (expandedPositions.contains(position)) {
                expandedPositions.remove(position)
                holder.layoutDetalles.visibility = View.GONE
            } else {
                expandedPositions.add(position)
                holder.layoutDetalles.visibility = View.VISIBLE
            }
        }

        // Restaurar estado al reciclar
        holder.layoutDetalles.visibility = if (expandedPositions.contains(position)) View.VISIBLE else View.GONE

        // Botones
        holder.btnEditar.setOnClickListener { onEditClick(comanda) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(comanda) }
    }

    override fun getItemCount() = comandas.size

    fun updateData(newComandas: List<Comanda>, newDetalles: Map<Int, List<PedidoDetalle>>) {
        comandas = newComandas
        detallesPorComanda = newDetalles
        expandedPositions.clear()
        notifyDataSetChanged()
    }

    private fun formatDetalle(comanda: Comanda, detalles: List<PedidoDetalle>): String {
        if (detalles.isEmpty()) return "Sin detalles"

        // Agrupar por persona
        val personas = detalles.groupBy { it.persona }
        val sb = StringBuilder()

        var totalGeneral = 0.0
        for ((persona, items) in personas) {
            val subtotal = items.sumOf { it.cantidad * it.precioUnitario }
            totalGeneral += subtotal
            sb.append("$persona  $${"%.2f".format(subtotal)}\n")
            for (item in items) {
                sb.append("${item.cantidad} x  ${getItemNombre(item.itemMenuId)}  $${"%.2f".format(item.cantidad * item.precioUnitario)}\n")
                if (!item.observaciones.isNullOrBlank()) {
                    sb.append("   \"${item.observaciones}\"\n")
                }
            }
            sb.append("\n")
        }
        sb.append("Total Pedido  $${"%.2f".format(totalGeneral)}")
        return sb.toString()
    }

    // Temporal: mientras no tengamos acceso al menú, usamos un mapa dummy.
    // Más adelante obtendremos el nombre desde la base de datos.
    private fun getItemNombre(itemMenuId: Int): String {
        return mapOf(
            1 to "Taco Carne Maíz",
            2 to "Taco Carne Harina",
            3 to "Taco Papa Maíz",
            4 to "Taco Papa Harina"
        )[itemMenuId] ?: "Item $itemMenuId"
    }
}