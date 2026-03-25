package com.example.minicomanda.ui.salas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Lobby(
    val lobbyId: String = "",
    val lobbyName: String = "",
    val isPrivate: Boolean = false,
    val password: String = ""
)

class LobbyViewModel : ViewModel() {
    private val _currentLobby = MutableLiveData<Lobby?>(null)
    val currentLobby: LiveData<Lobby?> = _currentLobby

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun joinLobby(lobby: Lobby) {
        _currentLobby.value = lobby
        showMessage("Te has unido al lobby: ${lobby.lobbyName}")
    }

    fun createLobby(lobby: Lobby) {
        // Simulación: generar ID único (más adelante será del servidor)
        val newLobby = lobby.copy(lobbyId = generateLobbyId())
        _currentLobby.value = newLobby
        showMessage("Lobby creado con ID: ${newLobby.lobbyId}")
    }

    fun leaveLobby() {
        _currentLobby.value = null
        showMessage("Has salido del lobby")
    }

    private fun showMessage(msg: String) {
        _message.value = msg
        // Opcional: limpiar mensaje después de un tiempo
        // _message.postValue("")
    }

    private fun generateLobbyId(): String {
        // Simulación: ID de 6 caracteres alfanuméricos
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}