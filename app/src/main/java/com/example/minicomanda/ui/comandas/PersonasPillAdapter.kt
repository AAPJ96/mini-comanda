package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.databinding.ItemPersonaPillBinding
import android.view.View
import android.content.Context

// PersonasPillAdapter.kt
class PersonasPillAdapter(
    private val personas: List<String>,
    private val selectedIndex: Int,
    private val onPersonaClick: (Int) -> Unit,
    private val onAddClick: () -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PersonasPillAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int): Int =
        if (position == personas.size) TYPE_ADD else TYPE_PERSONA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPersonaPillBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val isAddButton = position == personas.size

        with(holder.binding) {
            // Limpiar cualquier filtro previo
            btnDelete.clearColorFilter()

            if (isAddButton) {
                tvPersonaNombre.text = "+"
                btnDelete.visibility = View.GONE
                root.setOnClickListener { onAddClick() }

                // Estilo fijo: outline (blanco + borde azul)
                cardPersona.setCardBackgroundColor(context.getColor(android.R.color.white))
                cardPersona.strokeColor = context.getColor(R.color.blue)  // Int
                cardPersona.strokeWidth = 2.dpToPx(context)
                tvPersonaNombre.setTextColor(context.getColor(R.color.blue))
            } else {
                val persona = personas[position]
                tvPersonaNombre.text = persona
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener { onDeleteClick(position) }
                root.setOnClickListener { onPersonaClick(position) }

                val isSelected = position == selectedIndex
                if (isSelected) {
                    // Fondo azul, sin borde
                    cardPersona.setCardBackgroundColor(context.getColor(R.color.blue))
                    cardPersona.strokeColor = 0  // o 0
                    cardPersona.strokeWidth = 0
                    tvPersonaNombre.setTextColor(context.getColor(android.R.color.white))
                    btnDelete.setColorFilter(context.getColor(android.R.color.white))
                } else {
                    // Outline: blanco + borde azul
                    cardPersona.setCardBackgroundColor(context.getColor(android.R.color.white))
                    cardPersona.strokeColor = context.getColor(R.color.blue)
                    cardPersona.strokeWidth = 2.dpToPx(context)
                    tvPersonaNombre.setTextColor(context.getColor(R.color.blue))
                    btnDelete.setColorFilter(context.getColor(R.color.blue))
                }
            }
        }
    }

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    override fun getItemCount() = personas.size + 1

    class ViewHolder(val binding: ItemPersonaPillBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TYPE_PERSONA = 0
        private const val TYPE_ADD = 1
    }
}