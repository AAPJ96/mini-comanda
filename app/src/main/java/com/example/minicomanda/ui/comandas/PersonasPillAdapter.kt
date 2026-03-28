package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.databinding.ItemPersonaPillBinding

class PersonasPillAdapter(
    private val personas: List<String>,
    private val selectedIndex: Int,
    private val onPersonaClick: (Int) -> Unit,
    private val onAddClick: () -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PersonasPillAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (position == personas.size) TYPE_ADD else TYPE_PERSONA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ADD -> {
                val binding = ItemPersonaPillBinding.inflate(inflater, parent, false)
                ViewHolder(binding).apply {
                    binding.tvPersonaNombre.text = "+"
                    binding.btnDelete.visibility = android.view.View.GONE
                    binding.root.setOnClickListener { onAddClick() }
                }
            }
            else -> {
                val binding = ItemPersonaPillBinding.inflate(inflater, parent, false)
                ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < personas.size) {
            val persona = personas[position]
            holder.binding.tvPersonaNombre.text = persona
            holder.binding.btnDelete.visibility = android.view.View.VISIBLE

            // Estilo según selección
            if (position == selectedIndex) {
                holder.binding.cardPersona.setCardBackgroundColor(holder.itemView.context.getColor(R.color.blue))
                holder.binding.tvPersonaNombre.setTextColor(holder.itemView.context.getColor(android.R.color.white))
                holder.binding.btnDelete.setColorFilter(holder.itemView.context.getColor(android.R.color.white))
            } else {
                holder.binding.cardPersona.setCardBackgroundColor(holder.itemView.context.getColor(android.R.color.transparent))
                holder.binding.tvPersonaNombre.setTextColor(holder.itemView.context.getColor(R.color.blue))
                holder.binding.btnDelete.setColorFilter(holder.itemView.context.getColor(R.color.blue))
            }

            holder.binding.cardPersona.setOnClickListener { onPersonaClick(position) }
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(position) }
        }
    }

    override fun getItemCount() = personas.size + 1

    class ViewHolder(val binding: ItemPersonaPillBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TYPE_PERSONA = 0
        private const val TYPE_ADD = 1
    }
}