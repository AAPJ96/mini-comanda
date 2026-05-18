package com.example.minicomanda.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.minicomanda.data.local.entities.ItemMenu

@Dao
interface ItemMenuDao {

    // Insertar un ítem (si ya existe con ese UUID, reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: ItemMenu)

    // Actualizar campos de un ítem
    @Update
    suspend fun actualizar(item: ItemMenu)

    // Eliminación lógica (activo = false)
    @Query("UPDATE items_menu SET activo = 0, fecha_modificacion = :fechaMod WHERE id = :id")
    suspend fun eliminarLogicamente(id: String, fechaMod: Long)

    // Obtener un ítem por su ID (solo activos)
    @Query("SELECT * FROM items_menu WHERE id = :id AND activo = 1")
    suspend fun obtenerPorId(id: String): ItemMenu?

    // Listar todos los ítems activos de una sala, ordenados
    @Query("SELECT * FROM items_menu WHERE sala_id = :salaId AND activo = 1 ORDER BY orden_visualizacion, nombre")
    fun obtenerTodosDeSala(salaId: String): LiveData<List<ItemMenu>>

    // Reordenar: actualiza solo el campo orden_visualizacion
    @Query("UPDATE items_menu SET orden_visualizacion = :orden, fecha_modificacion = :fechaMod WHERE id = :id")
    suspend fun actualizarOrden(id: String, orden: Int, fechaMod: Long)

    // Obtener ítems que no han sido sincronizados con el servidor
    @Query("SELECT * FROM items_menu WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizados(): List<ItemMenu>
}