package com.example.minicomanda.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.minicomanda.data.local.entities.MenuItem

class MenuViewModel : ViewModel() {
    private val _menuItems = MutableLiveData<List<MenuItem>>(emptyList())
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    init {
        // Cargar datos dummy si la lista está vacía
        if (_menuItems.value.isNullOrEmpty()) {
            loadDummyData()
        }
    }

    private fun loadDummyData() {
        val dummy = listOf(
            MenuItem(1, "Taco Carne Maíz", 25.0),
            MenuItem(2, "Taco Carne Harina", 35.0),
            MenuItem(3, "Taco Papa Maiz", 30.0),
            MenuItem(4, "Taco Papa Harina", 45.0)
        )
        _menuItems.value = dummy
    }
    fun setMenuItems(items: List<MenuItem>) {
        _menuItems.value = items
    }

    fun addMenuItem(item: MenuItem) {
        val current = _menuItems.value?.toMutableList() ?: mutableListOf()
        current.add(item)
        _menuItems.value = current
    }

    fun removeMenuItem(item: MenuItem) {
        val current = _menuItems.value?.toMutableList() ?: return
        current.remove(item)
        _menuItems.value = current
    }

    fun updateMenuItem(updatedItem: MenuItem) {
        val index = _menuItems.value?.indexOfFirst { it.id == updatedItem.id } ?: -1
        if (index != -1) {
            val currentList = _menuItems.value?.toMutableList() ?: mutableListOf()
            currentList[index] = updatedItem
            _menuItems.value = currentList
            // Aquí luego se hará la actualización en Room
        }
    }

    fun moveItem(from: Int, to: Int) {
        val current = _menuItems.value?.toMutableList() ?: return
        if (from < to) {
            for (i in from until to) {
                current.add(i, current.removeAt(i + 1))
            }
        } else {
            for (i in from downTo to + 1) {
                current.add(i, current.removeAt(i - 1))
            }
        }
        _menuItems.value = current
    }
}