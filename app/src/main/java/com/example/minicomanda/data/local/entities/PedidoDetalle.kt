package com.example.minicomanda.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedido_detalles",
    foreignKeys = [ForeignKey(
        entity = Comanda::class,
        parentColumns = ["id"],
        childColumns = ["comandaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PedidoDetalle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val comandaId: Int,
    val persona: String,             // Ej: "Persona 1", "Persona 2" o nombre personalizado
    val itemMenuId: Int,             // referencia a MenuItem
    val cantidad: Int,
    val precioUnitario: Double,      // copia del precio al momento de crear el detalle
    val observaciones: String? = null // "sin cebolla"
)