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

    private val salaDao by lazy { MiniComandaApplication.instance.salaDao }

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
        val id = generarSalaId()
        val sala = Sala(
            id = id,
            nombre = nombre,
            esPrivada = esPrivada,
            contrasena = contrasena,
            configuracion = configuracion,
            sincronizado = false
        )
        viewModelScope.launch {
            salaDao.insertar(sala)
            guardarSesion(sala)
            showMessage("Sala creada: ${sala.nombre}")
        }
    }

    /** * CORRECCIÓN: Unirse a una sala existente devolviendo el resultado de la validación.
     * Al ser suspend, se ejecuta en el hilo de fondo y permite al Fragment esperar la respuesta.
     */
    suspend fun unirseASala(id: String, contrasenaIngresada: String?): Boolean {
        // 1. Buscamos la sala en la base de datos
        val sala = salaDao.obtenerPorId(id) ?: return false

        // 2. Si es privada, validamos que la contraseña coincida
        if (sala.esPrivada || !sala.contrasena.isNullOrEmpty()) {
            if (sala.contrasena != contrasenaIngresada) {
                return false // Contraseña incorrecta
            }
        }

        // 3. Si pasa los filtros, guardamos la sesión de forma persistente
        guardarSesion(sala)
        return true // Conexión exitosa
    }

    /** Salir de la sala actual */
    fun salirDeSala() {
        viewModelScope.launch {
            // Solo borramos la "llave" de la sesión actual, no tocamos la base de datos de Room
            prefs.edit()
                .remove("sala_id")
                .apply()

            _currentSala.value = null
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
                contrasena = nuevaContrasena ?: sala.contrasena,
                fechaModificacion = System.currentTimeMillis(),
                sincronizado = false
            )
            salaDao.actualizar(salaActualizada)
            _currentSala.value = salaActualizada
            showMessage("Sala actualizada")
        }
    }

    /** Reutiliza la consulta directa a Room */
    suspend fun obtenerSalaPorIdLocal(salaId: String): Sala? {
        return salaDao.obtenerPorId(salaId)
    }
}