package com.example.minicomanda.ui.comandas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
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
    private val _personasCount = MutableLiveData(1) // mínimo 1 persona
    val personasCount: LiveData<Int> = _personasCount

    private val _selectedPersonaIndex = MutableLiveData(0)
    val selectedPersonaIndex: LiveData<Int> = _selectedPersonaIndex

    val personas: LiveData<List<String>> = _personasCount.map { count ->
        (0 until count).map { "Persona ${it + 1}" }
    }

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
        val currentCount = _personasCount.value ?: 1
        val newIndex = currentCount
        _personasCount.value = currentCount + 1
        _pedidosPorPersona[newIndex] = mutableMapOf()
        _selectedPersonaIndex.value = newIndex
        // ya no se agrega a la lista de strings
        actualizarUi()
        recalcularTotales()
    }

    fun eliminarPersona(index: Int) {
        val currentCount = _personasCount.value ?: 1
        if (currentCount == 1) return // mínimo 1 persona

        // Reindexar mapa de pedidos (como antes)
        val newPedidos = mutableMapOf<Int, MutableMap<Int, PedidoDetalle>>()
        for (i in 0 until currentCount - 1) { // nueva cantidad será currentCount - 1
            val oldKey = if (i < index) i else i + 1
            newPedidos[i] = _pedidosPorPersona[oldKey] ?: mutableMapOf()
        }
        _pedidosPorPersona.clear()
        _pedidosPorPersona.putAll(newPedidos)

        // Actualizar cantidad
        _personasCount.value = currentCount - 1

        // Ajustar selección (similar a antes)
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
                persona = "Persona ${personaIndex + 1}",  // ← generado al vuelo
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
            direccion = if (paraLlevar) observaciones else null
        )

        val detalles = mutableListOf<PedidoDetalle>()
        val count = _personasCount.value ?: 1
        for (personaIndex in 0 until count) {
            val nombrePersona = "Persona ${personaIndex + 1}" // generado al vuelo
            val pedidos = _pedidosPorPersona[personaIndex] ?: continue
            for (detalle in pedidos.values) {
                detalles.add(
                    detalle.copy(
                        id = 0,
                        comandaId = 0,
                        persona = nombrePersona,
                        observaciones = null
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