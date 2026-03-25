package com.example.minicomanda.ui.comandas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.PedidoDetalle
import java.util.*

class ComandasViewModel : ViewModel() {
    private val _comandas = MutableLiveData<List<Comanda>>(emptyList())
    val comandas: LiveData<List<Comanda>> = _comandas

    private val _detalles = MutableLiveData<Map<Int, List<PedidoDetalle>>>(emptyMap())
    val detalles: LiveData<Map<Int, List<PedidoDetalle>>> = _detalles

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        // Datos de ejemplo
        val comanda1 = Comanda(
            id = 1,
            folio = "C-001",
            fecha = Date(),
            estado = "ACTIVA",
            nombreCliente = "Gorra azul",
            paraLlevar = false,
            total = 204.0
        )
        val comanda2 = Comanda(
            id = 2,
            folio = "C-002",
            fecha = Date(),
            estado = "ACTIVA",
            nombreCliente = "Rodolfo",
            paraLlevar = true,
            direccion = "Calle 123",
            total = 75.0
        )
        _comandas.value = listOf(comanda1, comanda2)

        val detallesMap = mutableMapOf<Int, List<PedidoDetalle>>()
        detallesMap[1] = listOf(
            PedidoDetalle(1, 1, "Persona 1", 1, 5, 25.0, null),
            PedidoDetalle(2, 1, "Persona 1", 1, 1, 25.0, null),
            PedidoDetalle(3, 1, "Persona 2", 2, 3, 18.0, "sin cebolla")
        )
        detallesMap[2] = listOf(
            PedidoDetalle(4, 2, "Persona 1", 3, 3, 25.0, null)
        )
        _detalles.value = detallesMap
    }

    fun addComanda(comanda: Comanda, detalles: List<PedidoDetalle>) {
        // Simulación: agregar a la lista (más adelante se hará en Room)
        val currentList = _comandas.value?.toMutableList() ?: mutableListOf()
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        val newComanda = comanda.copy(id = newId)
        currentList.add(newComanda)
        _comandas.value = currentList

        val currentDetalles = _detalles.value?.toMutableMap() ?: mutableMapOf()
        val nuevosDetalles = detalles.map { it.copy(id = 0, comandaId = newId) }
        currentDetalles[newId] = nuevosDetalles
        _detalles.value = currentDetalles
    }

    fun deleteComanda(comanda: Comanda) {
        val currentList = _comandas.value?.toMutableList() ?: return
        currentList.removeAll { it.id == comanda.id }
        _comandas.value = currentList

        val currentDetalles = _detalles.value?.toMutableMap() ?: return
        currentDetalles.remove(comanda.id)
        _detalles.value = currentDetalles
    }

    fun updateComanda(comanda: Comanda, detalles: List<PedidoDetalle>) {
        // Eliminar la antigua y agregar la nueva
        deleteComanda(comanda)
        addComanda(comanda, detalles)
    }
}