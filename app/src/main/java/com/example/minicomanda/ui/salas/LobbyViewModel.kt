package com.example.minicomanda.ui.salas

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minicomanda.MiniComandaApplication
import com.example.minicomanda.data.local.entities.Sala
import kotlinx.coroutines.launch

class LobbyViewModel(application: Application) : AndroidViewModel(application) {

    // Antes: private val salaDao = MiniComandaApplication.instance.salaDao
    private val salaDao by lazy { MiniComandaApplication.instance.salaDao }

    // Antes: private val prefs = application.getSharedPreferences(...)
    private val prefs by lazy {
        application.getSharedPreferences("minicomanda_prefs", Context.MODE_PRIVATE)
    }

    // Sala actualmente activa
    private val _currentSala = MutableLiveData<Sala?>()
    val currentSala: LiveData<Sala?> = _currentSala

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        // Al iniciar, intenta cargar la sala guardada en SharedPreferences
        cargarSalaDesdePrefs()
    }

    /** Carga la sala desde SharedPreferences si existe */
    private fun cargarSalaDesdePrefs() {
        val salaId = prefs.getString("sala_id", null) ?: return
        viewModelScope.launch {
            val sala = salaDao.obtenerPorId(salaId)
            if (sala != null) {
                _currentSala.value = sala
                showMessage("Sesión restaurada: ${sala.nombre}")
            } else {
                // La sala guardada ya no existe en la BD local
                prefs.edit().clear().apply()
            }
        }
    }

    /** Crear una nueva sala (localmente, sin conexión) */
    fun crearSala(nombre: String, esPrivada: Boolean, contrasena: String?, configuracion: String?) {
        val id = generarSalaId()   // puedes usar la misma función base62
        val sala = Sala(
            id = id,
            nombre = nombre,
            esPrivada = esPrivada,
            contrasena = contrasena,   // texto plano, hasta que se sincronice
            configuracion = configuracion,
            sincronizado = false
        )
        viewModelScope.launch {
            salaDao.insertar(sala)
            guardarSesion(sala)
            showMessage("Sala creada: ${sala.nombre}")
        }
    }

    /** Unirse a una sala existente (por ahora solo local) */
    fun unirseASala(id: String, contrasena: String?) {
        viewModelScope.launch {
            val sala = salaDao.obtenerPorId(id)
            if (sala == null) {
                showMessage("Sala no encontrada")
            } else if (sala.esPrivada && sala.contrasena != contrasena) {
                showMessage("Contraseña incorrecta")
            } else {
                guardarSesion(sala)
                showMessage("Te has unido a: ${sala.nombre}")
            }
        }
    }

    /** Salir de la sala actual */
    fun salirDeSala() {
        viewModelScope.launch {
            _currentSala.value?.let {
                salaDao.eliminarLogicamente(it.id, System.currentTimeMillis())
            }
            prefs.edit().clear().apply()
            _currentSala.value = null
            showMessage("Has salido de la sala")
        }
    }

    /** Guardar la sesión activa en SharedPreferences */
    private fun guardarSesion(sala: Sala) {
        prefs.edit()
            .putString("sala_id", sala.id)
            .putString("sala_nombre", sala.nombre)
            .putBoolean("sala_privada", sala.esPrivada)
            .apply()
        _currentSala.value = sala
    }

    private fun showMessage(msg: String) {
        _message.value = msg
    }

    /** Generador base62 simple (provisional) */
    private fun generarSalaId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    fun actualizarSala(nombre: String, esPrivada: Boolean, nuevaContrasena: String?) {
        viewModelScope.launch {
            val sala = _currentSala.value ?: return@launch
            val salaActualizada = sala.copy(
                nombre = nombre,
                esPrivada = esPrivada,
                contrasena = nuevaContrasena ?: sala.contrasena, // si no se pasó contraseña, mantener la anterior
                fechaModificacion = System.currentTimeMillis(),
                sincronizado = false
            )
            salaDao.actualizar(salaActualizada)
            _currentSala.value = salaActualizada
            showMessage("Sala actualizada")
        }
    }
}