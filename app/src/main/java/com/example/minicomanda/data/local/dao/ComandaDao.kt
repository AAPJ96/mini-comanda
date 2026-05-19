package com.example.minicomanda.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.minicomanda.data.local.entities.Comanda
import com.example.minicomanda.data.local.entities.ComandaConItems

@Dao
interface ComandaDao {

    // Insertar una comanda (si el UUID ya existe, reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(comanda: Comanda)

    // Actualizar campos de una comanda
    @Update
    suspend fun actualizar(comanda: Comanda)

    // Eliminación lógica (cancelar)
    @Query("UPDATE comandas SET activo = 0, estado = 'CANCELADA', fecha_modificacion = :fechaMod WHERE id = :id")
    suspend fun cancelar(id: String, fechaMod: Long)

    // Obtener una comanda por ID (solo activas)
    @Query("SELECT * FROM comandas WHERE id = :id AND activo = 1")
    suspend fun obtenerPorId(id: String): Comanda?

    // Listar todas las comandas activas de una sala, ordenadas por fecha descendente
    @Query("SELECT * FROM comandas WHERE sala_id = :salaId AND activo = 1 ORDER BY fecha_creacion DESC")
    fun obtenerTodasDeSala(salaId: String): LiveData<List<Comanda>>

    // Listar comandas por estado (ej. "ACTIVO", "PAGADO")
    @Query("SELECT * FROM comandas WHERE sala_id = :salaId AND activo = 1 AND estado = :estado ORDER BY fecha_creacion DESC")
    fun obtenerPorEstado(salaId: String, estado: String): LiveData<List<Comanda>>

    // Obtener comandas no sincronizadas
    @Query("SELECT * FROM comandas WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizadas(): List<Comanda>

    // Calcular el siguiente folio para una sala (máximo actual + 1)
    @Query("SELECT COALESCE(MAX(CAST(folio AS INTEGER)), 0) + 1 FROM comandas WHERE sala_id = :salaId")
    suspend fun siguienteFolio(salaId: String): Int

    @Transaction
    @Query("SELECT * FROM comandas WHERE sala_id = :salaId AND activo = 1 ORDER BY fecha_creacion DESC")
    fun obtenerComandasConItems(salaId: String): LiveData<List<ComandaConItems>>

    @Transaction
    @Query("SELECT * FROM comandas WHERE sala_id = :salaId AND activo = 1 ORDER BY fecha_creacion ASC")
    suspend fun obtenerComandasConItemsSync(salaId: String): List<ComandaConItems>

    @Transaction
    @Query("SELECT * FROM comandas WHERE sala_id = :salaId AND activo = 1 ORDER BY fecha_creacion DESC")
    suspend fun obtenerComandasConItemsDescSync(salaId: String): List<ComandaConItems>

    @Transaction
    @Query("SELECT * FROM comandas WHERE sala_id = :salaId AND estado = 'PAGADO' AND activo = 1 ORDER BY fecha_creacion DESC")
    fun obtenerComandasPagadasConItems(salaId: String): LiveData<List<ComandaConItems>>
}