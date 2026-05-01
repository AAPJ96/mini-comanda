package com.example.minicomanda.ui.comandas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.databinding.ItemPersonaPillBinding

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
        val chip = holder.binding.chipPersona

        if (position == personas.size) {
            // Chip de "+"
            chip.text = "+"
            chip.isCloseIconVisible = false
            chip.setOnClickListener { onAddClick() }
            // Estilo no seleccionado (outline)
            chip.setChipBackgroundColorResource(android.R.color.white)
            chip.setChipStrokeColorResource(R.color.blue)
            chip.setTextColor(context.getColor(R.color.blue))
            chip.chipStrokeWidth = 2f
            chip.isCheckable = false
        } else {
            // Chip de persona
            val persona = personas[position]
            chip.text = persona
            chip.isCloseIconVisible = true
            chip.isCheckable = true

            // Listener de eliminar
            chip.setOnCloseIconClickListener { onDeleteClick(position) }
            // Listener de selección (al tocar la chip)
            chip.setOnClickListener { onPersonaClick(position) }

            val isSelected = position == selectedIndex
            if (isSelected) {
                chip.isChecked = true  // activa el estado 'checked' y aplica colores definidos en el tema, pero los personalizamos abajo
                chip.setChipBackgroundColorResource(R.color.blue)
                chip.setChipStrokeColorResource(android.R.color.transparent)
                chip.chipStrokeWidth = 0f
                chip.setTextColor(context.getColor(android.R.color.white))
                chip.setCloseIconTintResource(android.R.color.white)
            } else {
                chip.isChecked = false
                chip.setChipBackgroundColorResource(android.R.color.white)
                chip.setChipStrokeColorResource(R.color.blue)
                chip.chipStrokeWidth = 2f
                chip.setTextColor(context.getColor(R.color.blue))
                chip.setCloseIconTintResource(R.color.blue)
            }
        }
    }

    override fun getItemCount() = personas.size + 1

    class ViewHolder(val binding: ItemPersonaPillBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TYPE_PERSONA = 0
        private const val TYPE_ADD = 1
    }
}