package com.example.minicomanda.ui.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ItemComandaConMenu
import com.example.minicomanda.data.local.entities.ComandaConItems
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val comandaDao by lazy { MiniComandaApplication.instance.comandaDao }

    private val salaId: String
        get() {
            val prefs = getApplication<MiniComandaApplication>()
                .getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
            return prefs.getString("sala_id", "") ?: ""
        }

    private val _comandasPagadas = MutableLiveData<List<Comanda>>(emptyList())
    val comandasPagadas: LiveData<List<Comanda>> = _comandasPagadas

    private val _detalles = MutableLiveData<Map<String, List<ItemComandaConMenu>>>(emptyMap())
    val detalles: LiveData<Map<String, List<ItemComandaConMenu>>> = _detalles

    private val _resumenHtml = MutableLiveData<String>()
    val resumenHtml: LiveData<String> = _resumenHtml

    // Caché local para guardar las comandas de la BD y poder reagruparlas rápido en memoria
    private var cacheComandasConItems: List<ComandaConItems> = emptyList()

    init {
        cargarHistorial()
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            val todasConItems = comandaDao.obtenerComandasConItemsDescSync(salaId)
            // Guardamos solo las pagadas
            cacheComandasConItems = todasConItems.filter { it.comanda.estado == "PAGADO" }

            // Alimentamos el listado normal de tarjetas individuales
            _comandasPagadas.value = cacheComandasConItems.map { it.comanda }
            _detalles.value = cacheComandasConItems.associate { it.comanda.id to it.items }

            // Por defecto, generamos el resumen agrupado por "Diario"
            generarResumenAgrupado("Diario")
        }
    }

    /**
     * Se llama desde el Fragment cuando el usuario cambia el Spinner (Diario, Mensual, Anual)
     */
    fun cambiarAgrupacion(nuevaAgrupacion: String) {
        generarResumenAgrupado(nuevaAgrupacion)
    }

    private fun generarResumenAgrupado(agrupacion: String) {
        if (cacheComandasConItems.isEmpty()) {
            _resumenHtml.value = "<h1>Resumen Histórico</h1><p>No hay comandas pagadas en esta sala todavía.</p>"
            return
        }

        // 1. Agrupamos usando llaves ordenables alfabéticamente (AAAA-MM-DD) para no perder el orden cronológico
        val grupos = cacheComandasConItems.groupBy { conItems ->
            val cal = Calendar.getInstance().apply { timeInMillis = conItems.comanda.fechaCreacion }
            when (agrupacion) {
                "Mensual" -> String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                "Anual" -> String.format(Locale.US, "%04d", cal.get(Calendar.YEAR))
                else -> String.format(Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
            }
        }

        // 2. Ordenamos las llaves de forma Descendente (lo más nuevo primero)
        val clavesOrdenadas = grupos.keys.sortedDescending()

        val builder = StringBuilder()
        builder.append("<h1>Resumen Histórico ($agrupacion)</h1>")
        builder.append("<p>Se encontraron ${grupos.size} períodos con actividad.</p>")

        // 3. Procesamos cada grupo para armar su propia "tarjeta de resultados"
        for (clave in clavesOrdenadas) {
            val comandasDelPeriodo = grupos[clave] ?: emptyList()

            // Traducimos la llave técnica (ej: "2026-05") a un formato legible (ej: "Mayo 2026")
            val tituloPeriodo = when (agrupacion) {
                "Mensual" -> {
                    val partes = clave.split("-")
                    val mesInt = partes[1].toInt()
                    val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
                    "${meses[mesInt - 1]} ${partes[0]}"
                }
                "Anual" -> clave
                else -> { // Diario ("2026-05-18" -> "18/05/2026")
                    val partes = clave.split("-")
                    "${partes[2]}/${partes[1]}/${partes[0]}"
                }
            }

            // Inicializamos los acumuladores para este bloque específico
            val totalComandas = comandasDelPeriodo.size
            var totalCentavos = 0L
            val conteoPlatillos = mutableMapOf<String, Int>()

            for (comandaConItems in comandasDelPeriodo) {
                for (itemConMenu in comandaConItems.items) {
                    val pedido = itemConMenu.itemComanda
                    val menu = itemConMenu.itemMenu

                    totalCentavos += (pedido.cantidad * pedido.precioOriginalUnidad)
                    conteoPlatillos[menu.nombre] = (conteoPlatillos[menu.nombre] ?: 0) + pedido.cantidad
                }
            }

            val totalDinero = totalCentavos / 100.0
            // Tomamos los 3 más vendidos de este período específico
            val topPlatillos = conteoPlatillos.toList()
                .sortedByDescending { it.second }
                .take(3)

            // 4. Inyectamos la estructura visual de la "tarjeta" en el HTML
            builder.append("<br>")
            builder.append("<h3><b>Período: $tituloPeriodo</b></h3>")
            builder.append("<p><b>• Órdenes finalizadas:</b> $totalComandas</p>")
            builder.append("<p><b>• Ingresos del período:</b> $${"%.2f".format(totalDinero)}</p>")
            builder.append("<p><b>• Lo más vendido:</b></p>")

            if (topPlatillos.isEmpty()) {
                builder.append("<p><i>Sin platillos registrados</i></p>")
            } else {
                builder.append("<ul>")
                for ((platillo, cantidad) in topPlatillos) {
                    builder.append("<li>$platillo ($cantidad unidades)</li>")
                }
                builder.append("</ul>")
            }
            // Línea divisoria estética para separar las tarjetas simuladas
            builder.append("<p>───────────────────────────</p>")
        }

        _resumenHtml.value = builder.toString()
    }
}