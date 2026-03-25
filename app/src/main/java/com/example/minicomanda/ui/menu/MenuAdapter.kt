package com.example.minicomanda.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minicomanda.R
import com.example.minicomanda.data.local.entities.MenuItem
import com.google.android.material.imageview.ShapeableImageView

class MenuAdapter(
    private var items: MutableList<MenuItem>,
    private val onEditClick: (MenuItem) -> Unit,
    private val onDeleteClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ShapeableImageView = itemView.findViewById(R.id.iv_item_image)
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.nombre
        holder.tvPrice.text = "$${"%.2f".format(item.precio)}"
        holder.ivImage.setImageResource(R.drawable.placeholder)

        // Al hacer clic en la card se muestra el menú contextual
        holder.itemView.setOnClickListener {
            showPopupMenu(holder.itemView, item)
        }
    }

    private fun showPopupMenu(view: View, item: MenuItem) {
        // Usamos el ContextThemeWrapper para aplicar el estilo al contexto antes de crear el Popup
        val wrapper = androidx.appcompat.view.ContextThemeWrapper(view.context, R.style.CustomPopupMenu)
        val popup = PopupMenu(wrapper, view)

        popup.menuInflater.inflate(R.menu.menu_item_options, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    onEditClick(item)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(item)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<MenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                items.add(i, items.removeAt(i + 1))
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                items.add(i, items.removeAt(i - 1))
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getCurrentList(): List<MenuItem> = items
}
