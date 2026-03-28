package com.example.minicomanda.ui.comandas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.MenuItem
import com.example.minicomanda.data.local.entities.PedidoDetalle
import com.example.minicomanda.ui.menu.MenuViewModel

class AgregarComandaViewModel(private val menuViewModel: MenuViewModel) : ViewModel() {

    // Datos del cliente
    private val _nombreCliente = MutableLiveData("")
    val nombreCliente: LiveData<String> = _nombreCliente

    // Tipo de pedido
    private val _paraLlevar = MutableLiveData(false)
    val paraLlevar: LiveData<Boolean> = _paraLlevar

    // Observaciones
    private val _observaciones = MutableLiveData("")
    val observaciones: LiveData<String> = _observaciones

    // Lista de personas
    private val _personas = MutableLiveData<List<String>>(listOf("Persona 1"))
    val personas: LiveData<List<String>> = _personas

    private val _selectedPersonaIndex = MutableLiveData(0)
    val selectedPersonaIndex: LiveData<Int> = _selectedPersonaIndex

    // Mapa: índice de persona -> (itemMenuId -> PedidoDetalle)
    private val _pedidosPorPersona = mutableMapOf<Int, MutableMap<Int, PedidoDetalle>>()

    // Items del menú (observado desde MenuViewModel)
    private val _menuItems = MutableLiveData<List<MenuItem>>(emptyList())
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    // Lista de pedidos por persona para la UI (se actualizará automáticamente)
    private val _pedidosPorPersonaUi = MutableLiveData<Map<Int, Map<Int, PedidoDetalle>>>(emptyMap())
    val pedidosPorPersonaUi: LiveData<Map<Int, Map<Int, PedidoDetalle>>> = _pedidosPorPersonaUi

    // Total general
    private val _totalGeneral = MutableLiveData(0.0)
    val totalGeneral: LiveData<Double> = _totalGeneral

    init {
        // Observar cambios en el menú
        menuViewModel.menuItems.observeForever { items ->
            _menuItems.value = items
        }

        // Inicializar pedidos para la primera persona
        _pedidosPorPersona[0] = mutableMapOf()
        actualizarUi()
        recalcularTotales()
    }

    fun setNombreCliente(nombre: String) {
        _nombreCliente.value = nombre
    }

    fun setParaLlevar(value: Boolean) {
        _paraLlevar.value = value
    }

    fun setObservaciones(text: String) {
        _observaciones.value = text
    }

    fun agregarPersona() {
        val current = _personas.value?.toMutableList() ?: return
        val newIndex = current.size
        current.add("Persona ${newIndex + 1}")
        _personas.value = current
        _pedidosPorPersona[newIndex] = mutableMapOf()
        // Seleccionar la nueva persona
        _selectedPersonaIndex.value = newIndex
        actualizarUi()
        recalcularTotales()
    }

    fun eliminarPersona(index: Int) {
        val current = _personas.value?.toMutableList() ?: return
        if (current.size == 1) return // al menos una persona debe existir
        current.removeAt(index)
        _personas.value = current

        // Reindexar mapa de pedidos (desplazar los índices mayores)
        val newPedidos = mutableMapOf<Int, MutableMap<Int, PedidoDetalle>>()
        for (i in current.indices) {
            val oldKey = if (i < index) i else i + 1
            newPedidos[i] = _pedidosPorPersona[oldKey] ?: mutableMapOf()
        }
        _pedidosPorPersona.clear()
        _pedidosPorPersona.putAll(newPedidos)

        // Ajustar selección
        if (_selectedPersonaIndex.value == index) {
            _selectedPersonaIndex.value = if (index > 0) index - 1 else 0
        } else if (_selectedPersonaIndex.value!! > index) {
            _selectedPersonaIndex.value = _selectedPersonaIndex.value!! - 1
        }
        actualizarUi()
        recalcularTotales()
    }

    fun seleccionarPersona(index: Int) {
        _selectedPersonaIndex.value = index
        // No necesitamos actualizar UI aquí porque la selección solo afecta a las píldoras
    }

    fun agregarItemAMenu(menuItem: MenuItem) {
        val personaIndex = _selectedPersonaIndex.value ?: return
        val pedidosPersona = _pedidosPorPersona[personaIndex] ?: return

        val detalleActual = pedidosPersona[menuItem.id]
        if (detalleActual != null) {
            // Incrementar cantidad
            val nuevoDetalle = detalleActual.copy(
                cantidad = detalleActual.cantidad + 1,
                precioUnitario = menuItem.precio // actualizar por si cambió
            )
            pedidosPersona[menuItem.id] = nuevoDetalle
        } else {
            // Crear nuevo detalle
            val nuevoDetalle = PedidoDetalle(
                comandaId = 0,
                persona = _personas.value!![personaIndex],
                itemMenuId = menuItem.id,
                cantidad = 1,
                precioUnitario = menuItem.precio,
                observaciones = null
            )
            pedidosPersona[menuItem.id] = nuevoDetalle
        }
        actualizarUi()
        recalcularTotales()
    }

    fun incrementarItem(personaIndex: Int, menuItem: MenuItem) {
        // Forzamos seleccionar la persona para mantener consistencia (opcional)
        seleccionarPersona(personaIndex)
        agregarItemAMenu(menuItem)
    }

    fun decrementarItem(personaIndex: Int, menuItem: MenuItem) {
        val pedidosPersona = _pedidosPorPersona[personaIndex] ?: return
        val detalleActual = pedidosPersona[menuItem.id] ?: return

        if (detalleActual.cantidad == 1) {
            pedidosPersona.remove(menuItem.id)
        } else {
            val nuevoDetalle = detalleActual.copy(cantidad = detalleActual.cantidad - 1)
            pedidosPersona[menuItem.id] = nuevoDetalle
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
            for (detalle in pedidos.values) {
                total += detalle.cantidad * detalle.precioUnitario
            }
        }
        _totalGeneral.value = total
    }

    fun construirComanda(): Pair<Comanda, List<PedidoDetalle>> {
        val folio = generarFolio()
        val fecha = java.util.Date()
        val nombreCliente = _nombreCliente.value ?: ""
        val total = _totalGeneral.value ?: 0.0
        val paraLlevar = _paraLlevar.value ?: false
        val observaciones = _observaciones.value ?: ""

        val comanda = Comanda(
            id = 0,
            folio = folio,
            fecha = fecha,
            estado = "ACTIVA",
            nombreCliente = nombreCliente,
            paraLlevar = paraLlevar,
            total = total,
            pagado = false,
            direccion = if (paraLlevar) observaciones else null // usamos observaciones como dirección temporal
        )

        val detalles = mutableListOf<PedidoDetalle>()
        for ((personaIndex, pedidos) in _pedidosPorPersona) {
            val nombrePersona = _personas.value!![personaIndex]
            for (detalle in pedidos.values) {
                detalles.add(
                    detalle.copy(
                        id = 0,
                        comandaId = 0,
                        persona = nombrePersona,
                        observaciones = null // podríamos permitir observaciones por item
                    )
                )
            }
        }
        return Pair(comanda, detalles)
    }

    private fun generarFolio(): String {
        val timestamp = System.currentTimeMillis()
        return "C-${timestamp % 100000}"
    }
}