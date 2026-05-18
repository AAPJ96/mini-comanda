package com.example.minicomanda.ui.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ItemComanda
import com.example.minicomanda.data.local.entities.ComandaConItems

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val comandaDao by lazy { MiniComandaApplication.instance.comandaDao }

    private val salaId: String
        get() {
            val prefs = getApplication<MiniComandaApplication>()
                .getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)
            return prefs.getString("sala_id", "") ?: ""
        }

    // Comandas pagadas con sus ítems
    private val _comandasConItems: LiveData<List<ComandaConItems>> =
        comandaDao.obtenerComandasPagadasConItems(salaId)

    // Lista de comandas pagadas (sin ítems)
    val comandasPagadas: LiveData<List<Comanda>> = _comandasConItems.map { lista ->
        lista.map { it.comanda }
    }

    // Mapa de detalles (comandaId -> List<ItemComanda>)
    val detalles: LiveData<Map<String, List<ItemComanda>>> = _comandasConItems.map { lista ->
        lista.associate { it.comanda.id to it.items }
    }

    // Mensaje opcional (para Toasts)
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje
}