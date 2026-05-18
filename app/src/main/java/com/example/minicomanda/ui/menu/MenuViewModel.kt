package com.example.minicomanda.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.ItemMenu
import kotlinx.coroutines.launch

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val itemMenuDao by lazy { MiniComandaApplication.instance.itemMenuDao }

    // Lista observable de ítems del menú (se actualiza automáticamente desde Room)
    val menuItems: LiveData<List<ItemMenu>> = itemMenuDao.obtenerTodosDeSala(
        obtenerSalaIdActiva()
    )

    // Mensaje de retroalimentación (para Toasts, por ejemplo)
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    /**
     * Obtiene el ID de la sala activa desde SharedPreferences.
     * Si no hay sala activa, devuelve una cadena vacía (pero la app debería
     * redirigir a la pantalla de salas).
     */
    private fun obtenerSalaIdActiva(): String {
        val prefs = getApplication<MiniComandaApplication>()
            .getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("sala_id", "") ?: ""
    }

    /** Agregar un nuevo ítem al menú */
    fun agregarItem(item: ItemMenu) {
        viewModelScope.launch {
            // Asignar sala activa y fecha de creación
            val nuevoItem = item.copy(
                salaId = obtenerSalaIdActiva(),
                fechaCreacion = System.currentTimeMillis(),
                fechaModificacion = System.currentTimeMillis(),
                activo = true,
                sincronizado = false
            )
            itemMenuDao.insertar(nuevoItem)
            _mensaje.postValue("Item agregado: ${item.nombre}")
        }
    }

    /** Actualizar un ítem existente */
    fun actualizarItem(item: ItemMenu) {
        viewModelScope.launch {
            val actualizado = item.copy(
                fechaModificacion = System.currentTimeMillis(),
                sincronizado = false
            )
            itemMenuDao.actualizar(actualizado)
            _mensaje.postValue("Item actualizado: ${item.nombre}")
        }
    }

    /** Eliminar (lógico) un ítem */
    fun eliminarItem(item: ItemMenu) {
        viewModelScope.launch {
            itemMenuDao.eliminarLogicamente(item.id, System.currentTimeMillis())
            _mensaje.postValue("Item eliminado: ${item.nombre}")
        }
    }

    /** Reordenar: actualiza solo el campo ordenVisualizacion */
    fun actualizarOrden(itemId: String, nuevoOrden: Int) {
        viewModelScope.launch {
            itemMenuDao.actualizarOrden(itemId, nuevoOrden, System.currentTimeMillis())
        }
    }

    /** Mover un ítem de una posición a otra (para drag & drop) */
    fun moverItem(from: Int, to: Int) {
        val lista = menuItems.value?.toMutableList() ?: return
        if (from < 0 || from >= lista.size || to < 0 || to >= lista.size) return

        val itemMovido = lista.removeAt(from)
        lista.add(to, itemMovido)

        // Actualizar el orden de todos los ítems afectados
        viewModelScope.launch {
            lista.forEachIndexed { index, item ->
                if (item.ordenVisualizacion != index) {
                    itemMenuDao.actualizarOrden(item.id, index, System.currentTimeMillis())
                }
            }
        }
    }
}