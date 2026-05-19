package com.example.minicomanda.ui.comandas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ItemComanda
import com.example.minicomanda.data.local.entities.ItemMenu
import kotlinx.coroutines.launch

class EditarComandaViewModel(application: Application, private val comandaId: String) : AndroidViewModel(application) {

    private val comandaDao by lazy { MiniComandaApplication.instance.comandaDao }
    private val itemComandaDao by lazy { MiniComandaApplication.instance.itemComandaDao }
    private val itemMenuDao by lazy { MiniComandaApplication.instance.itemMenuDao }

    // ─── Datos de la comanda ───
    private val _nombreCliente = MutableLiveData("")
    val nombreCliente: LiveData<String> = _nombreCliente

    private val _paraLlevar = MutableLiveData(false)
    val paraLlevar: LiveData<Boolean> = _paraLlevar

    private val _observaciones = MutableLiveData("")
    val observaciones: LiveData<String> = _observaciones

    private val _estado = MutableLiveData("ACTIVO")
    val estado: LiveData<String> = _estado

    private val _pagado = MutableLiveData(false)
    val pagado: LiveData<Boolean> = _pagado

    val folio: String?
        get() = _comandaOriginal?.folio?.toString()

    private var _comandaOriginal: Comanda? = null

    // ─── Personas ───
    private val _personasCount = MutableLiveData(1)
    val personasCount: LiveData<Int> = _personasCount

    val personas: LiveData<List<String>> = _personasCount.map { count ->
        (0 until count).map { "Persona ${it + 1}" }
    }

    private val _selectedPersonaIndex = MutableLiveData(0)
    val selectedPersonaIndex: LiveData<Int> = _selectedPersonaIndex

    // Mapa: índice persona -> (itemMenuId -> ItemComanda)
    private val _pedidosPorPersona = mutableMapOf<Int, MutableMap<String, ItemComanda>>()
    private val _pedidosPorPersonaUi = MutableLiveData<Map<Int, Map<String, ItemComanda>>>()
    val pedidosPorPersonaUi: LiveData<Map<Int, Map<String, ItemComanda>>> = _pedidosPorPersonaUi

    private val _totalGeneral = MutableLiveData(0.0)
    val totalGeneral: LiveData<Double> = _totalGeneral

    // Menú observable
    private val _menuItems = MutableLiveData<List<ItemMenu>>(emptyList())
    val menuItems: LiveData<List<ItemMenu>> = _menuItems

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    init {
        // Cargar el menú de la sala activa
        val salaId = obtenerSalaIdActiva()
        itemMenuDao.obtenerTodosDeSala(salaId).observeForever { items ->
            _menuItems.value = items
        }
        // Cargar la comanda y sus items
        cargarComandaExistente()
    }

    private fun obtenerSalaIdActiva(): String {
        val prefs = getApplication<MiniComandaApplication>()
            .getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("sala_id", "") ?: ""
    }

    private fun cargarComandaExistente() {
        viewModelScope.launch {
            val comanda = comandaDao.obtenerPorId(comandaId) ?: return@launch
            _comandaOriginal = comanda

            _nombreCliente.value = comanda.comensal
            _paraLlevar.value = comanda.esParaLlevar
            _observaciones.value = comanda.notas ?: ""
            _estado.value = comanda.estado
            _pagado.value = comanda.estado == "PAGADO"

            // Obtener todos los ítems activos de la comanda
            val items = itemComandaDao.obtenerTodosDeComandaSync(comandaId)

            // Determinar cuántas personas hay (máximo número de persona en los ítems)
            val personaMax = items.maxOfOrNull { it.persona } ?: 1
            _personasCount.value = personaMax

            // Inicializar los mapas por persona
            for (i in 0 until personaMax) {
                _pedidosPorPersona[i] = mutableMapOf()
            }

            // Rellenar los ítems en el mapa correspondiente
            for (item in items) {
                val personaIndex = item.persona - 1  // persona va de 1..N, índice de 0..N-1
                _pedidosPorPersona[personaIndex]?.put(item.itemMenuId, item)
            }

            actualizarUi()
            recalcularTotales()
        }
    }

    // ─── Funciones de manipulación (idénticas a AgregarComandaViewModel) ───
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

    fun seleccionarPersona(index: Int) { _selectedPersonaIndex.value = index }

    fun agregarItemAMenu(menuItem: ItemMenu) {
        val personaIndex = _selectedPersonaIndex.value ?: return
        val pedidosPersona = _pedidosPorPersona[personaIndex] ?: return

        val actual = pedidosPersona[menuItem.id]
        if (actual != null) {
            pedidosPersona[menuItem.id] = actual.copy(cantidad = actual.cantidad + 1)
        } else {
            pedidosPersona[menuItem.id] = ItemComanda(
                id = java.util.UUID.randomUUID().toString(),
                comandaId = comandaId,
                itemMenuId = menuItem.id,
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

    fun marcarPagado() {
        _estado.value = "PAGADO"
        _pagado.value = true
    }

    private fun actualizarUi() {
        _pedidosPorPersonaUi.value = _pedidosPorPersona.mapValues { it.value.toMap() }
    }

    private fun recalcularTotales() {
        var total = 0.0
        for ((_, pedidos) in _pedidosPorPersona) {
            for (item in pedidos.values) {
                total += item.cantidad * (item.precioOriginalUnidad / 100.0)
            }
        }
        _totalGeneral.value = total
    }

    // ─── Construir la comanda actualizada ───
    fun construirComandaActualizada(): Pair<Comanda, List<ItemComanda>> {
        val original = _comandaOriginal ?: throw IllegalStateException("Comanda no cargada")
        val nombreCliente = _nombreCliente.value ?: ""
        val paraLlevar = _paraLlevar.value ?: false
        val observaciones = _observaciones.value ?: ""
        val totalCentavos = _totalGeneral.value?.let { (it * 100).toLong() } ?: 0L

        val comanda = original.copy(
            comensal = nombreCliente,
            esParaLlevar = paraLlevar,
            // CORRECCIÓN: Guardamos las notas siempre. Si está en blanco, mandamos null.
            notas = observaciones.ifBlank { null },
            personas = _personasCount.value ?: 1,
            estado = _estado.value ?: "ACTIVO",
            fechaModificacion = System.currentTimeMillis(),
            sincronizado = false
        )

        val items = mutableListOf<ItemComanda>()
        val count = _personasCount.value ?: 1
        for (personaIndex in 0 until count) {
            val pedidos = _pedidosPorPersona[personaIndex] ?: emptyMap()
            for (item in pedidos.values) {
                items.add(
                    item.copy(
                        comandaId = comanda.id,
                        persona = personaIndex + 1,
                        fechaModificacion = System.currentTimeMillis(),
                        sincronizado = false
                    )
                )
            }
        }
        return Pair(comanda, items)
    }

    // Dentro de la clase EditarComandaViewModel

    fun cancelarComanda() {
        viewModelScope.launch {
            comandaDao.cancelar(comandaId, System.currentTimeMillis())
            _mensaje.postValue("Comanda cancelada")
        }
    }

    class Factory(private val app: Application, private val comandaId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditarComandaViewModel(app, comandaId) as T
        }
    }
}