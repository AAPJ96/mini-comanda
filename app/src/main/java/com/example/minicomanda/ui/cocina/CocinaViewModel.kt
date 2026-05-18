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
import com.example.minicomanda.data.local.entities.ItemComanda
import com.example.minicomanda.data.local.entities.ComandaConItems
import kotlinx.coroutines.launch

/**
 * Wrapper que asocia una comanda activa con sus ítems y datos de temporizador.
 */
data class ComandaCocina(
    val comanda: Comanda,
    val detalles: List<ItemComanda>,
    val tiempoLimiteMs: Long = 20 * 60 * 1000L,  // 20 min default
    val timestampInicio: Long = System.currentTimeMillis(),
    val itemsPreparados: MutableSet<String> = mutableSetOf()  // IDs de ItemComanda
) {
    fun getProgreso(): Float {
        val transcurrido = System.currentTimeMillis() - timestampInicio
        return (transcurrido.toFloat() / tiempoLimiteMs).coerceIn(0f, 1.2f)
    }

    fun estaCompletado(): Boolean = detalles.all { it.id in itemsPreparados }
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
            // Obtener todas las comandas activas de la sala
            val comandasActivas = comandaDao.obtenerComandasConItemsSync(salaId)
                .filter { it.comanda.estado == "ACTIVO" }

            // Convertir a ComandaCocina y asignar timestamp de inicio (por ahora, el momento actual)
            val listaCocina = comandasActivas.map { conItems ->
                ComandaCocina(
                    comanda = conItems.comanda,
                    detalles = conItems.items,
                    timestampInicio = conItems.comanda.fechaCreacion  // usar fecha real de creación
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
            itemComandaDao.marcarItemListo(itemId, System.currentTimeMillis())
            // Actualizar también el conjunto local para reflejar inmediatamente el checkbox
            val listaActual = _comandas.value?.toMutableList() ?: return@launch
            val comandaCocina = listaActual.getOrNull(comandaIndex) ?: return@launch
            if (listo) {
                comandaCocina.itemsPreparados.add(itemId)
            } else {
                comandaCocina.itemsPreparados.remove(itemId)
            }
            _comandas.value = listaActual
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