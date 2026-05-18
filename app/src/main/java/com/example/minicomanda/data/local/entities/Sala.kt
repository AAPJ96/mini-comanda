package com.example.minicomanda.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salas")
data class Sala(
    @PrimaryKey
    val id: String,                    // CHAR(8) base62
    val nombre: String,
    @ColumnInfo(name = "es_privada")
    val esPrivada: Boolean = true,
    val contrasena: String? = null,    // contraseña en texto plano (solo si no está sincronizada)
    val configuracion: String? = null,  // JSON como texto
    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "fecha_modificacion")
    val fechaModificacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true,
    val sincronizado: Boolean = false
)