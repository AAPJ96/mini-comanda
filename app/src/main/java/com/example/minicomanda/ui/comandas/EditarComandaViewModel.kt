package com.example.minicomanda.ui.comandas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.minicomanda.data.local.entities.MenuItem
import com.example.minicomanda.data.local.entities.PedidoDetalle
import com.example.minicomanda.ui.menu.MenuViewModel

class EditarComandaViewModel(
    private val menuViewModel: MenuViewModel,
    private val folioComanda: String
) : ViewModel() {

    // Datos del cliente
    private val _nombreCliente = MutableLiveData("")
    val nombreCliente: LiveData<String> = _nombreCliente

    private val _paraLlevar = MutableLiveData(false)
    val paraLlevar: LiveData<Boolean> = _paraLlevar

    private val _observaciones = MutableLiveData("")
    val observaciones: LiveData<String> = _observaciones

    // Estado y pagado (solo para edición)
    private val _estado = MutableLiveData("ACTIVA")
    val estado: LiveData<String> = _estado

    private val _pagado = MutableLiveData(false)
    val pagado: LiveData<Boolean> = _pagado

    // Gestión de personas
    private val _personasCount = MutableLiveData(1)
    val personasCount: LiveData<Int> = _personasCount

    private val _selectedPersonaIndex = MutableLiveData(0)
    val selectedPersonaIndex: LiveData<Int> = _selectedPersonaIndex

    val personas: LiveData<List<String>> = _personasCount.map { count ->
        (0 until count).map { "Persona ${it + 1}" }
    }

    // Pedidos por persona usando directamente PedidoDetalle
    private val _pedidosPorPersona = mutableMapOf<Int, MutableMap<Int, PedidoDetalle>>()
    private val _pedidosPorPersonaUi = MutableLiveData<Map<Int, Map<Int, PedidoDetalle>>>()
    val pedidosPorPersonaUi: LiveData<Map<Int, Map<Int, PedidoDetalle>>> = _pedidosPorPersonaUi

    private val _totalGeneral = MutableLiveData(0.0)
    val totalGeneral: LiveData<Double> = _totalGeneral

    // Exponemos el menú para los adaptadores
    private val _menuItems = MutableLiveData<List<MenuItem>>(emptyList())
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    val folio: String = folioComanda

    init {
        // Observar menú y copiarlo a nuestro LiveData local
        menuViewModel.menuItems.observeForever { items ->
            _menuItems.value = items
        }
        cargarDatosDummy(folioComanda)
    }

    private fun cargarDatosDummy(folio: String) {
        when (folio) {
            "C-001" -> {
                _nombreCliente.value = "Gorra azul"
                _paraLlevar.value = false
                _estado.value = "ACTIVA"
                _pagado.value = false
                _personasCount.value = 2

                // Persona 0 (Persona 1)
                _pedidosPorPersona[0] = mutableMapOf(
                    1 to PedidoDetalle(
                        id = 0, comandaId = 0, persona = "Persona 1",
                        itemMenuId = 1, cantidad = 5, precioUnitario = 25.0
                    ),
                    2 to PedidoDetalle(
                        id = 0, comandaId = 0, persona = "Persona 1",
                        itemMenuId = 2, cantidad = 1, precioUnitario = 25.0
                    )
                )
                // Persona 1 (Persona 2)
                _pedidosPorPersona[1] = mutableMapOf(
                    3 to PedidoDetalle(
                        id = 0, comandaId = 0, persona = "Persona 2",
                        itemMenuId = 3, cantidad = 2, precioUnitario = 18.0
                    )
                )
            }
            "C-002" -> {
                _nombreCliente.value = "Rodolfo"
                _paraLlevar.value = true
                _observaciones.value = "Calle 123"
                _estado.value = "ACTIVA"
                _pagado.value = false
                _personasCount.value = 1

                _pedidosPorPersona[0] = mutableMapOf(
                    3 to PedidoDetalle(
                        id = 0, comandaId = 0, persona = "Persona 1",
                        itemMenuId = 3, cantidad = 3, precioUnitario = 25.0
                    )
                )
            }
            else -> {
                _nombreCliente.value = "Cliente $folio"
                _personasCount.value = 1
                _pedidosPorPersona[0] = mutableMapOf()
            }
        }
        actualizarUi()
        recalcularTotales()
    }

    // Métodos de manipulación

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

        val newPedidos = mutableMapOf<Int, MutableMap<Int, PedidoDetalle>>()
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

    fun agregarItemAMenu(menuItem: MenuItem) {
        val personaIndex = _selectedPersonaIndex.value ?: return
        val pedidosPersona = _pedidosPorPersona[personaIndex] ?: return

        val detalleActual = pedidosPersona[menuItem.id]
        if (detalleActual != null) {
            pedidosPersona[menuItem.id] = detalleActual.copy(
                cantidad = detalleActual.cantidad + 1,
                precioUnitario = menuItem.precio
            )
        } else {
            pedidosPersona[menuItem.id] = PedidoDetalle(
                comandaId = 0,
                persona = "Persona ${personaIndex + 1}",
                itemMenuId = menuItem.id,
                cantidad = 1,
                precioUnitario = menuItem.precio,
                observaciones = null
            )
        }
        actualizarUi()
        recalcularTotales()
    }

    fun incrementarItem(personaIndex: Int, menuItem: MenuItem) {
        seleccionarPersona(personaIndex)
        agregarItemAMenu(menuItem)
    }

    fun decrementarItem(personaIndex: Int, menuItem: MenuItem) {
        val pedidosPersona = _pedidosPorPersona[personaIndex] ?: return
        val detalleActual = pedidosPersona[menuItem.id] ?: return
        if (detalleActual.cantidad == 1) {
            pedidosPersona.remove(menuItem.id)
        } else {
            pedidosPersona[menuItem.id] = detalleActual.copy(cantidad = detalleActual.cantidad - 1)
        }
        actualizarUi()
        recalcularTotales()
    }

    fun cerrarCuenta() {
        _estado.value = "CERRADA"
    }

    fun marcarPagado() {
        _pagado.value = true
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

    // Setters para los campos de texto
    fun setNombreCliente(nombre: String) { _nombreCliente.value = nombre }
    fun setParaLlevar(value: Boolean) { _paraLlevar.value = value }
    fun setObservaciones(text: String) { _observaciones.value = text }
}