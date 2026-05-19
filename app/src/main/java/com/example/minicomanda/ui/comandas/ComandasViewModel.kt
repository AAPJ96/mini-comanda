package com.example.minicomanda.ui.comandas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.map
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ItemComanda
import com.example.minicomanda.data.local.entities.ComandaConItems
import com.example.minicomanda.data.local.entities.ItemComandaConMenu
import kotlinx.coroutines.launch

class ComandasViewModel(application: Application) : AndroidViewModel(application) {

    private val comandaDao by lazy { MiniComandaApplication.instance.comandaDao }
    private val itemComandaDao by lazy { MiniComandaApplication.instance.itemComandaDao }

    private val salaId: String
        get() {
            val prefs = getApplication<MiniComandaApplication>()
                .getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
            return prefs.getString("sala_id", "") ?: ""
        }

    // Lista original directa desde la base de datos
    private val _comandasConItems: LiveData<List<ComandaConItems>> =
        comandaDao.obtenerComandasConItems(salaId)

    // NUEVO: Capa intermedia de filtrado reactivo
    private val _comandasFiltradasUi: LiveData<List<ComandaConItems>> = _comandasConItems.map { lista ->
        lista.filter { conItems ->
            val estadoComanda = conItems.comanda.estado

            // Condición A: Mostrar siempre si está ACTIVA
            val esActiva = estadoComanda == "ACTIVO"

            // Condición B: Si está PAGADA, mostrar solo si hay ítems que NO están listos
            val esPagadaPeroPendiente = estadoComanda == "PAGADO" &&
                    conItems.items.any { it.itemComanda.estado != "LISTO" }

            esActiva || esPagadaPeroPendiente
        }
    }

    // Exponemos las comandas solas, alimentadas de la lista ya filtrada
    val comandas: LiveData<List<Comanda>> = _comandasFiltradasUi.map { lista ->
        lista.map { it.comanda }
    }

    // Exponemos el mapa de detalles, alimentado de la lista ya filtrada
    val detalles: LiveData<Map<String, List<ItemComandaConMenu>>> = _comandasFiltradasUi.map { lista ->
        lista.associate { it.comanda.id to it.items }
    }

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    /** Agregar una nueva comanda (creada desde AgregarComandaFragment) */
    fun agregarComanda(comanda: Comanda, items: List<ItemComanda>) {
        viewModelScope.launch {
            // Asignar sala activa y sincronizado = false
            val nuevaComanda = comanda.copy(
                salaId = salaId,
                sincronizado = false,
                fechaCreacion = System.currentTimeMillis(),
                fechaModificacion = System.currentTimeMillis()
            )
            comandaDao.insertar(nuevaComanda)

            // Insertar cada ítem de la comanda
            items.forEach { item ->
                itemComandaDao.insertar(
                    item.copy(
                        comandaId = nuevaComanda.id,
                        sincronizado = false,
                        fechaCreacion = System.currentTimeMillis(),
                        fechaModificacion = System.currentTimeMillis()
                    )
                )
            }
            _mensaje.postValue("Comanda ${comanda.folio ?: ""} guardada")
        }
    }

    /** Actualizar comanda existente (desde EditarComandaFragment) */
    /** Actualizar comanda existente (desde EditarComandaFragment) */
    fun actualizarComanda(comanda: Comanda, items: List<ItemComanda>) {
        viewModelScope.launch {
            val actualizada = comanda.copy(
                fechaModificacion = System.currentTimeMillis(),
                sincronizado = false
            )
            comandaDao.actualizar(actualizada)

            // 1. Obtener los ítems como están guardados actualmente en la BD antes de borrarlos
            val itemsAnteriores = itemComandaDao.obtenerItemsPorComandaSync(comanda.id)

            // Creamos un mapa rápido de asignación: id del ítem -> cantidad guardada
            val mapaCantidadesAnteriores = itemsAnteriores.associate { it.id to it.cantidad }

            // 2. Eliminamos los ítems antiguos para reescribir
            itemComandaDao.eliminarTodosDeComanda(comanda.id, System.currentTimeMillis())

            // 3. Insertar cada ítem aplicando la regla de validación de cantidad
            items.forEach { item ->
                val cantidadAnterior = mapaCantidadesAnteriores[item.id]

                // CONDICIÓN REQUERIDA: Si el ítem ya existía pero cambió su cantidad, se reinicia
                val nuevoEstado = if (cantidadAnterior != null && cantidadAnterior != item.cantidad) {
                    "EN_PREPARACION"
                } else {
                    item.estado // Si no cambió la cantidad, mantiene su estado original (LISTO o EN_PREPARACION)
                }

                itemComandaDao.insertar(
                    item.copy(
                        comandaId = comanda.id,
                        estado = nuevoEstado, // <-- Asignamos el estado recalculado
                        fechaModificacion = System.currentTimeMillis(),
                        sincronizado = false
                    )
                )
            }
            _mensaje.postValue("Comanda actualizada")
        }
    }

    fun cancelarComanda(comandaId: String) {
        viewModelScope.launch {
            comandaDao.cancelar(comandaId, System.currentTimeMillis())
            _mensaje.postValue("Comanda cancelada")
        }
    }
}