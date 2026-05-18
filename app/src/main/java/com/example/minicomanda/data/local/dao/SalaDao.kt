package com.example.minicomanda.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.minicomanda.data.local.entities.Sala

@Dao
interface SalaDao {

    // Insertar una nueva sala (si ya existe con ese ID, la reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sala: Sala)

    // Actualizar campos de una sala existente
    @Update
    suspend fun actualizar(sala: Sala)

    // Eliminación lógica (poner activo = false)
    @Query("UPDATE salas SET activo = 0, fecha_modificacion = :fechaMod WHERE id = :id")
    suspend fun eliminarLogicamente(id: String, fechaMod: Long)

    // Obtener una sala por su ID (solo si está activa)
    @Query("SELECT * FROM salas WHERE id = :id AND activo = 1")
    suspend fun obtenerPorId(id: String): Sala?

    // Obtener todas las salas activas (observable con LiveData)
    @Query("SELECT * FROM salas WHERE activo = 1")
    fun obtenerTodasActivas(): LiveData<List<Sala>>

    // Obtener salas que no han sido sincronizadas con el servidor
    @Query("SELECT * FROM salas WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizadas(): List<Sala>

    // marcar como sincronizada y borrar la contraseña
    @Query("UPDATE salas SET sincronizado = 1, contrasena = NULL WHERE id = :id")
    suspend fun marcarSincronizada(id: String)
}