package com.example.minicomanda.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "comandas")
data class Comanda(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val folio: String,               // Ej: "C-001"
    val fecha: Date,
    val estado: String,              // "ACTIVA" o "CERRADA"
    val nombreCliente: String,       // "Gorra azul" o "Rodolfo"
    val paraLlevar: Boolean,         // true = para llevar, false = comer aquí
    val direccion: String? = null,   // solo si paraLlevar
    val total: Double,
    val pagado: Boolean = false      // indica si ya se pagó
)