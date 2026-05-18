package com.example.minicomanda.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_menu")
data class ItemMenu(
    @PrimaryKey
    val id: String,                    // UUID como texto (facilita trabajar con él)
    @ColumnInfo(name = "sala_id")
    val salaId: String,
    val nombre: String,
    val precio: Long,                  // en centavos (entero)
    val imagen: ByteArray? = null,     // LONGBLOB
    val descripcion: String? = null,
    val categoria: String? = null,
    @ColumnInfo(name = "es_modificador")
    val esModificador: Boolean = false,
    @ColumnInfo(name = "orden_visualizacion")
    val ordenVisualizacion: Int = 0,
    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "fecha_modificacion")
    val fechaModificacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true,
    val sincronizado: Boolean = false
):java.io.Serializable