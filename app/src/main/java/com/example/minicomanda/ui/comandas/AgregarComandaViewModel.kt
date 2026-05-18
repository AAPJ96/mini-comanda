package com.example.minicomanda.ui.comandas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ItemComanda
import com.example.minicomanda.data.local.entities.ItemMenu
import kotlinx.coroutines.launch

class AgregarComandaViewModel(application: Application) : AndroidViewModel(application) {

    private val itemMenuDao by lazy { MiniComandaApplication.instance.itemMenuDao }

    // ─── Datos del cliente ───
    private val _nombreCliente = MutableLiveData("")
    val nombreCliente: LiveData<String> = _nombreCliente

    private val _paraLlevar = MutableLiveData(false)
    val paraLlevar: LiveData<Boolean> = _paraLlevar

    private val _observaciones = MutableLiveData("")
    val observaciones: LiveData<String> = _observaciones

    // ─── Personas ───
    private val _personasCount = MutableLiveData(1)
    val personasCount: LiveData<Int> = _personasCount

    val personas: LiveData<List<String>> = _personasCount.map { count ->
        (0 until count).map { "Persona ${it + 1}" }
    }

    private val _selectedPersonaIndex = MutableLiveData(0)
    val selectedPersonaIndex: LiveData<Int> = _selectedPersonaIndex

    // Mapa: índice de persona -> (itemMenuId -> ItemComanda temporal)
    private val _pedidosPorPersona = mutableMapOf<Int, MutableMap<String, ItemComanda>>()

    // ─── Menú observable ───
    private val _menuItems = MutableLiveData<List<ItemMenu>>(emptyList())
    val menuItems: LiveData<List<ItemMenu>> = _menuItems

    // Pedidos por persona para la UI
    private val _pedidosPorPersonaUi = MutableLiveData<Map<Int, Map<String, ItemComanda>>>(emptyMap())
    val pedidosPorPersonaUi: LiveData<Map<Int, Map<String, ItemComanda>>> = _pedidosPorPersonaUi

    private val _totalGeneral = MutableLiveData(0.0)
    val totalGeneral: LiveData<Double> = _totalGeneral

    init {
        // Cargar el menú de la sala activa
        val salaId = obtenerSalaIdActiva()
        itemMenuDao.obtenerTodosDeSala(salaId).observeForever { items ->
            _menuItems.value = items
        }

        // Inicializar pedidos para la primera persona
        _pedidosPorPersona[0] = mutableMapOf()
        actualizarUi()
        recalcularTotales()
    }

    private fun obtenerSalaIdActiva(): String {
        val prefs = getApplication<MiniComandaApplication>()
            .getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("sala_id", "") ?: ""
    }

    // ─── Funciones de manipulación de personas ───
    fun setNombreCliente(nombre: String) { _nombreCliente.value = nombre }
    fun setParaLlevar(value: Boolean) { _paraLlevar.value = value }
    fun setObservaciones(text: String) { _observaciones.value = text }

    fun agregarPersona() {
        val currentCount = _personasCount.value ?: 1
        val newIndex = currentCount
        _personasCount.value = currentCount + 1
        _pedidosPorPersona[newIndex] = mutableMapOf()
        _selectedPersonaIndex.value = newIndex
        actualizarUi()
        recalcularTotales()
    }

    fun eliminarPersona(index: Int) {
        val currentCount = _personasCount.value ?: 1
        if (currentCount == 1) return
        val newPedidos = mutableMapOf<Int, MutableMap<String, ItemComanda>>()
        for (i in 0 until currentCount - 1) {
            val oldKey = if (i < index) i else i + 1
            newPedidos[i] = _pedidosPorPersona[oldKey] ?: mutableMapOf()
        }
        _pedidosPorPersona.clear()
        _pedidosPorPersona.putAll(newPedidos)
        _personasCount.value = currentCount - 1
        if (_selectedPersonaIndex.value == index) {
            _selectedPersonaIndex.value = if (index > 0) index - 1 else 0
        } else if ((_selectedPersonaIndex.value ?: 0) > index) {
            _selectedPersonaIndex.value = (_selectedPersonaIndex.value ?: 0) - 1
        }
        actualizarUi()
        recalcularTotales()
    }

    fun seleccionarPersona(index: Int) {
        _selectedPersonaIndex.value = index
    }

    // ─── Añadir / quitar ítems del menú ───
    fun agregarItemAMenu(menuItem: ItemMenu) {
        val personaIndex = _selectedPersonaIndex.value ?: return
        val pedidosPersona = _pedidosPorPersona[personaIndex] ?: return

        val actual = pedidosPersona[menuItem.id]   // menuItem.id es String
        if (actual != null) {
            pedidosPersona[menuItem.id] = actual.copy(cantidad = actual.cantidad + 1)
        } else {
            pedidosPersona[menuItem.id] = ItemComanda(
                id = java.util.UUID.randomUUID().toString(),
                comandaId = "",
                itemMenuId = menuItem.id,         // String
                cantidad = 1,
                persona = personaIndex + 1,
                precioOriginalUnidad = menuItem.precio,
                estado = "EN PREPARACION",
                sincronizado = false
            )
        }
        actualizarUi()
        recalcularTotales()
    }

    fun incrementarItem(personaIndex: Int, menuItem: ItemMenu) {
        seleccionarPersona(personaIndex)
        agregarItemAMenu(menuItem)
    }

    fun decrementarItem(personaIndex: Int, menuItem: ItemMenu) {
        val pedidosPersona = _pedidosPorPersona[personaIndex] ?: return
        val actual = pedidosPersona[menuItem.id] ?: return
        if (actual.cantidad == 1) {
            pedidosPersona.remove(menuItem.id)
        } else {
            pedidosPersona[menuItem.id] = actual.copy(cantidad = actual.cantidad - 1)
        }
        actualizarUi()
        recalcularTotales()
    }

    private fun actualizarUi() {
        _pedidosPorPersonaUi.value = _pedidosPorPersona.mapValues { it.value.toMap() }
    }

    private fun recalcularTotales() {
        var total = 0.0
        for ((_, pedidos) in _pedidosPorPersona) {
            for (item in pedidos.values) {
                total += item.cantidad * (item.precioOriginalUnidad / 100.0)  // convertir centavos a double
            }
        }
        _totalGeneral.value = total
    }

    // ─── Construir la comanda final (para ser guardada por ComandasViewModel) ───
    fun construirComanda(): Pair<Comanda, List<ItemComanda>> {
        val folio = null  // se asignará tras sincronización
        val fecha = System.currentTimeMillis()
        val nombreCliente = _nombreCliente.value ?: ""
        val totalCentavos = _totalGeneral.value?.let { (it * 100).toLong() } ?: 0L
        val paraLlevar = _paraLlevar.value ?: false
        val observaciones = _observaciones.value ?: ""

        val comanda = Comanda(
            id = java.util.UUID.randomUUID().toString(),
            salaId = obtenerSalaIdActiva(),
            folio = null,
            comensal = nombreCliente,
            personas = _personasCount.value ?: 1,
            esParaLlevar = paraLlevar,
            notas = if (paraLlevar) observaciones else null,
            estado = "ACTIVO",
            fechaCreacion = fecha,
            fechaModificacion = fecha,
            activo = true,
            sincronizado = false
        )

        val items = mutableListOf<ItemComanda>()
        val count = _personasCount.value ?: 1
        for (personaIndex in 0 until count) {
            val nombrePersona = "Persona ${personaIndex + 1}"
            val pedidos = _pedidosPorPersona[personaIndex] ?: emptyMap()
            for (item in pedidos.values) {
                items.add(
                    item.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        comandaId = comanda.id,
                        persona = personaIndex + 1
                    )
                )
            }
        }
        return Pair(comanda, items)
    }
}