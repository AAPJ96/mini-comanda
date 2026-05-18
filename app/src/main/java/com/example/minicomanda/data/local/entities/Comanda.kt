package com.example.minicomanda.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comandas")
data class Comanda(
    @PrimaryKey
    val id: String,                    // UUID como texto
    @ColumnInfo(name = "sala_id")
    val salaId: String,
    val folio: Int? = null,                 // folio como cadena (en servidor es BIGINT, aquí manejamos string para flexibilidad)
    val comensal: String? = null,      // nombre del cliente
    val personas: Int = 1,
    @ColumnInfo(name = "es_para_llevar")
    val esParaLlevar: Boolean = false,
    val mesa: Int? = null,
    val notas: String? = null,         // antes datos_entrega
    val estado: String = "ACTIVO",     // ACTIVO, PAGADO, CANCELADA
    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "fecha_modificacion")
    val fechaModificacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true,
    val sincronizado: Boolean = false
)