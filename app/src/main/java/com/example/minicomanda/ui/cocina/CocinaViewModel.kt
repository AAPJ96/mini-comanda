package com.example.minicomanda.ui.cocina

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ItemComandaConMenu
import kotlinx.coroutines.launch

/**
 * Wrapper que asocia una comanda activa con sus ítems y datos de temporizador.
 */
data class ComandaCocina(
    val comanda: Comanda,
    val detalles: List<ItemComandaConMenu>,
    val tiempoLimiteMs: Long = 20 * 60 * 1000L,  // 20 min default
    val timestampInicio: Long = System.currentTimeMillis(),
    val itemsPreparados: MutableSet<String> = mutableSetOf()  // IDs de ItemComanda
) {
    fun getProgreso(): Float {
        val transcurrido = System.currentTimeMillis() - timestampInicio
        return (transcurrido.toFloat() / tiempoLimiteMs).coerceIn(0f, 1.2f)
    }

    fun estaCompletado(): Boolean = detalles.all { it.itemComanda.id in itemsPreparados }
}

class CocinaViewModel(application: Application) : AndroidViewModel(application) {

    private val comandaDao by lazy { MiniComandaApplication.instance.comandaDao }
    private val itemComandaDao by lazy { MiniComandaApplication.instance.itemComandaDao }

    // Lista de comandas activas en la cocina
    private val _comandas = MutableLiveData<List<ComandaCocina>>(emptyList())
    val comandas: LiveData<List<ComandaCocina>> = _comandas

    // Handler para actualización periódica de la barra de progreso
    private val handler = Handler(Looper.getMainLooper())
    private var autoUpdateRunnable: Runnable? = null

    // Evento para avisarle al Fragmento que debe mostrar el Snackbar de Deshacer
    private val _eventoComandaCompletada = MutableLiveData<String?>()
    val eventoComandaCompletada: LiveData<String?> = _eventoComandaCompletada

    // Función para limpiar el evento y que no se vuelva a mostrar si se gira la pantalla
    fun eventoDeshacerMostrado() {
        _eventoComandaCompletada.value = null
    }
    private val salaId: String
        get() {
            val prefs = getApplication<MiniComandaApplication>()
                .getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
            return prefs.getString("sala_id", "") ?: ""
        }

    init {
        cargarComandasActivas()
        startPeriodicUpdate()
    }

    private fun cargarComandasActivas() {
        viewModelScope.launch {
            val comandasActivas = comandaDao.obtenerComandasConItemsSync(salaId)
                .filter { it.comanda.estado == "ACTIVO" || it.comanda.estado == "PAGADO" }
                // NUEVO FILTRO: Solo dejamos pasar las comandas que tengan AL MENOS UN ítem pendiente
                .filter { conItems ->
                    conItems.items.any { it.itemComanda.estado != "LISTO" }
                }

            val listaCocina = comandasActivas.map { conItems ->
                ComandaCocina(
                    comanda = conItems.comanda,
                    detalles = conItems.items,
                    timestampInicio = conItems.comanda.fechaCreacion
                )
            }
            _comandas.value = listaCocina
        }
    }

    /**
     * Necesitamos una versión síncrona de obtenerComandasConItems para usar dentro de corrutina.
     * Agregaremos este método en ComandaDao.
     */
    // Se añade en ComandaDao:
    // @Query("SELECT * FROM comandas WHERE sala_id = :salaId AND activo = 1 ORDER BY fecha_creacion DESC")
    // suspend fun obtenerComandasConItemsSync(salaId: String): List<ComandaConItems>

    private fun startPeriodicUpdate() {
        val runnable = object : Runnable {
            override fun run() {
                // Forzar notificación para refrescar las barras (no modifica los datos)
                _comandas.value = _comandas.value?.toList()
                handler.postDelayed(this, 200)  // cada 200ms
            }
        }
        handler.post(runnable)
    }

    fun marcarItemPreparado(comandaIndex: Int, itemId: String, listo: Boolean) {
        viewModelScope.launch {
            val nuevoEstado = if (listo) "LISTO" else "EN_PREPARACION"

            // LÓGICA DE DESHACER: Si lo estamos marcando como LISTO, ¿es el último que faltaba?
            if (listo) {
                val comandaActual = _comandas.value?.getOrNull(comandaIndex)
                if (comandaActual != null) {
                    // Revisamos si TODOS los demás ítems ya están LISTOS
                    val esElUltimo = comandaActual.detalles.all {
                        it.itemComanda.id == itemId || it.itemComanda.estado == "LISTO"
                    }
                    if (esElUltimo) {
                        // ¡Era el último! Disparamos el evento pasándole el ID de este ítem
                        _eventoComandaCompletada.value = itemId
                    }
                }
            }

            itemComandaDao.actualizarEstadoItem(itemId, nuevoEstado, System.currentTimeMillis())
            cargarComandasActivas()
        }
    }

    // Función que llamará el botón "Deshacer" del Snackbar
    fun deshacerItem(itemId: String) {
        viewModelScope.launch {
            itemComandaDao.actualizarEstadoItem(itemId, "EN_PREPARACION", System.currentTimeMillis())
            cargarComandasActivas() // Al recargar, la comanda reaparecerá
        }
    }

    /** Llamado cuando todos los ítems están listos (podría ocultar la comanda) */
    fun limpiarCompletadas() {
        val listaActual = _comandas.value?.filter { !it.estaCompletado() } ?: return
        _comandas.value = listaActual
    }

    override fun onCleared() {
        handler.removeCallbacksAndMessages(null)
        super.onCleared()
    }
}