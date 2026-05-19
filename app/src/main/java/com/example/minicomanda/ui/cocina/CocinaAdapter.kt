package com.example.minicomanda.ui.cocina

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R

class CocinaAdapter(
    private var comandas: List<ComandaCocina>,
    private val onItemCheckChange: (comandaIndex: Int, detalleId: String, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<CocinaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFolio: TextView = itemView.findViewById(R.id.tvFolioCocina)
        val tvCliente: TextView = itemView.findViewById(R.id.tvClienteCocina)

        val tvTipoPedido: TextView = itemView.findViewById(R.id.tvTipoPedidoCocina)

        val tvNotas: TextView = itemView.findViewById(R.id.tvNotasCocina)
        val layoutItems: LinearLayout = itemView.findViewById(R.id.layoutItemsCocina)
        val barraProgreso: ProgressBar = itemView.findViewById(R.id.barraProgreso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comanda_cocina, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comandaCocina = comandas[position]
        val context = holder.itemView.context

        holder.tvFolio.text = comandaCocina.comanda.folio?.toString() ?: "Sin folio"
        holder.tvCliente.text = comandaCocina.comanda.comensal ?: ""

        holder.tvTipoPedido.text = if (comandaCocina.comanda.esParaLlevar) "Para llevar" else "Comer aquí"

        val notas = comandaCocina.comanda.notas
        if (!notas.isNullOrBlank()) {
            holder.tvNotas.visibility = View.VISIBLE
            holder.tvNotas.text = "Notas: $notas"
        } else {
            holder.tvNotas.visibility = View.GONE
        }
        // Barra de progreso
        val progreso = comandaCocina.getProgreso()
        val remaining = (1f - progreso).coerceIn(0f, 1f)
        val color = when {
            remaining >= 0.75f -> context.getColor(R.color.green)
            remaining >= 0.5f -> context.getColor(R.color.yellow)
            remaining >= 0.25f -> context.getColor(R.color.orange)
            else -> context.getColor(R.color.red)
        }
        holder.barraProgreso.progress = (remaining * 100).toInt()
        holder.barraProgreso.progressTintList = ColorStateList.valueOf(color)

        // Items
        holder.layoutItems.removeAllViews()
        for (item in comandaCocina.detalles) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_detalle_cocina, holder.layoutItems, false)
            val tvCantidadNombre = itemView.findViewById<TextView>(R.id.tvCantidadNombre)
            val cbListo = itemView.findViewById<CheckBox>(R.id.cbListo)

            // Usamos las nuevas relaciones
            val datosPedido = item.itemComanda
            val datosPlatillo = item.itemMenu

            // Asignamos el texto real
            tvCantidadNombre.text = "${datosPedido.cantidad}x ${datosPlatillo.nombre}"

            // 1. Apagamos el listener temporalmente para evitar bugs al scrollear
            cbListo.setOnCheckedChangeListener(null)

            // 2. Evaluamos directamente el estado en la base de datos
            cbListo.isChecked = (datosPedido.estado == "LISTO")

            // 3. Volvemos a prender el listener
            cbListo.setOnCheckedChangeListener { _, isChecked ->
                onItemCheckChange(position, datosPedido.id, isChecked)
            }

            holder.layoutItems.addView(itemView)
        }
    }

    override fun getItemCount() = comandas.size

    fun updateComandas(nuevas: List<ComandaCocina>) {
        comandas = nuevas
        notifyDataSetChanged()
    }
}