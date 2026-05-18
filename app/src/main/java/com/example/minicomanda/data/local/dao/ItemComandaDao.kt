package com.example.minicomanda.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.minicomanda.data.local.entities.ItemComanda

@Dao
interface ItemComandaDao {

    // Insertar un nuevo ítem de comanda
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: ItemComanda)

    // Actualizar un ítem existente (cantidad, estado, etc.)
    @Update
    suspend fun actualizar(item: ItemComanda)

    // Eliminación lógica (quitar ítem de la comanda)
    @Query("UPDATE items_comanda SET activo = 0, fecha_modificacion = :fechaMod WHERE id = :id")
    suspend fun eliminarLogicamente(id: String, fechaMod: Long)

    // Obtener un ítem por su ID (solo activos)
    @Query("SELECT * FROM items_comanda WHERE id = :id AND activo = 1")
    suspend fun obtenerPorId(id: String): ItemComanda?

    // Listar todos los ítems activos de una comanda, ordenados por persona
    @Query("SELECT * FROM items_comanda WHERE comanda_id = :comandaId AND activo = 1 ORDER BY persona, fecha_creacion")
    fun obtenerTodosDeComanda(comandaId: String): LiveData<List<ItemComanda>>

    // Marcar un ítem como "LISTO" (atajo para cocina)
    @Query("UPDATE items_comanda SET estado = 'LISTO', fecha_modificacion = :fechaMod WHERE id = :id")
    suspend fun marcarItemListo(id: String, fechaMod: Long)

    // Obtener ítems que no han sido sincronizados
    @Query("SELECT * FROM items_comanda WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizados(): List<ItemComanda>

    // En ItemComandaDao
    @Query("UPDATE items_comanda SET activo = 0, fecha_modificacion = :fechaMod WHERE comanda_id = :comandaId")
    suspend fun eliminarTodosDeComanda(comandaId: String, fechaMod: Long)

    @Query("SELECT * FROM items_comanda WHERE comanda_id = :comandaId AND activo = 1 ORDER BY persona, fecha_creacion")
    suspend fun obtenerTodosDeComandaSync(comandaId: String): List<ItemComanda>
}